package com.aperture.docx.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import models.PageContent;

import org.docx4j.openpackaging.exceptions.Docx4JException;

// use play cache
import play.cache.Cache;

// use play log
import play.Logger;

import com.aperture.docx.templating.Module;
import com.aperture.docx.templating.ModuleCompiler;
import com.aperture.docx.templating.ModuleIO;

public class DocxTemplatingService {
	public enum DocType {
		DOC("doc"), MODULE("module");
		private final String type;

		DocType(String t) {
			this.type = t;
		}

		String getType() {
			return type;
		}
	}

	private static Object cast_impl(Object o) {
		if (o instanceof Module.ModuleModel) {
			Module.ModuleModel mm = (Module.ModuleModel) o;
			models.template.Module m = new models.template.Module(mm.name,
					mm.text, null);
			
			m.id = mm.id;
			m.list = new ArrayList<Object>();
			for (Object mo : mm.children) {
				Object casted = cast_impl(mo);
				if (casted != null)
					m.list.add(casted);
			}

			return m;
		} else if (o instanceof Module.QuestionModel) {
			Module.QuestionModel qm = (Module.QuestionModel) o;
			return new models.template.Question(qm.questionId, qm.context);
		}
		return null;
	}

	public static models.template.Module cast(Module.ModuleModel m) {
		return (models.template.Module) cast_impl(m);
	}

	public static void parseDocument(DocType type, String name, String path)
			throws Docx4JException {
		name = name.replaceAll("\\.docx$", "");
		long id = ModuleIO.newDocument(name, path);
		
		List<Long> updatingList = new ArrayList<Long>();
		updatingList.add(Long.valueOf(id));
		
		PageContent.updatePaging(updatingList);

		if (type == DocType.DOC) {
			List<models.File> files = models.File.find.where().eq("name", name)
					.findList();
			// not found, insert
			if (files.size() == 0) {
				models.File file = new models.File(name, type.getType(), id);
				file.save();
			}
		}
	}
	// ************************************************************************************
	
	// module compilers
	private static interface WithModuleCompiler<V> {
		V apply(ModuleCompiler mc, Module root) throws Docx4JException;
	}
	
	// use this as pattern
	private static <V> V compileModule(long id, Map<String, String> answers, 
	WithModuleCompiler<V> todo)throws Docx4JException {
		
		/*
		ModuleCompiler mc = new ModuleCompiler();
		Module m = ModuleIO.loadModule(id);

		if (m != null) {
			mc.pendModule(m);
			
			String path = settings.Constant.USER_DIR + "/" + m.getName() + ".docx";
			mc.save(path);

			return path;
		}

		return null;*/
			
		
		Module m = ModuleIO.loadModule(id);
		if ( m != null){
			ModuleCompiler mc = new ModuleCompiler();
			mc.pendModule(m);
			
			if ( answers != null ){
				mc.detemplate(answers);
			}
			
			return todo.apply(mc, m);
		}
		
		return null;
	}

	public static String getCompiledModule(long id) throws Docx4JException {
		return compileModule(id, null, new WithModuleCompiler<String>(){
			public String apply(ModuleCompiler mc, Module root) throws Docx4JException {
				String path = settings.Constant.USER_DIR + "/" + root.getName() + ".docx";
				mc.save(path);

				return path;	
			}
		});
	}
	
	public static String getFinalDoc(long id, Map<String, String> answers) throws Docx4JException {
		return compileModule(id, answers, new WithModuleCompiler<String>(){
			public String apply(ModuleCompiler mc, Module root) throws Docx4JException {
				return mc.convertToPdf("generated_" + root.getUpdateTag());
			}
		});
	}
	
	public static String getPdfPreview(long id) throws Docx4JException {
		// cache key
		final String tag = ModuleIO.loadModuleUpdateTag(id);
		// check
		if ( tag == null ) {
			Logger.warn("document module id:"+id+" not found.");
			return null;
		}
		
		java.io.File cached = new java.io.File(settings.Constant.USER_DIR + "/preview_" + tag + ".pdf");
		if ( cached.isFile() && cached.canRead() ){
			return "/preview/preview_" + tag;
		}
		
		// not found, generate it
		return compileModule(id, null, new WithModuleCompiler<String>(){
			public String apply(ModuleCompiler mc, Module root) throws Docx4JException{
				mc.convertToPdf("preview_" + root.getUpdateTag(), settings.Constant.USER_DIR);

				return "/preview/preview_" + root.getUpdateTag();
			}
		});
	}

	public static models.template.Module analyzeModule(long id)
			throws Docx4JException, java.lang.Exception {
		Module m = ModuleIO.loadModule(id);
		if (m != null) {
			String tag = m.getUpdateTag();
			final Module thisModule = m;
			
			return Cache.getOrElse("analyse_"+tag, new Callable<models.template.Module>(){
				public models.template.Module call(){
					return cast(thisModule.analyse());
				}
			}, 12 * 3600);
		}

		return null;
	}
	
	public static String generateQuestionUUID() {
		return UUID.randomUUID().toString();
	}
}