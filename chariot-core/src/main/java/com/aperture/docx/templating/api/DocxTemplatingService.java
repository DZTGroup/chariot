package com.aperture.docx.templating.api;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;

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

	public static boolean getCompiledModule(long id) throws Docx4JException {
		ModuleCompiler mc = new ModuleCompiler();
		Module m = ModuleIO.loadModule(id);

		if (m != null) {
			mc.pendModule(m);
			mc.save(settings.Constant.USER_DIR + "/" + m.getName() + ".docx");

			return true;
		}

		return false;
	}

	public static models.template.Module analyzeModule(long id)
			throws Docx4JException {
		Module m = ModuleIO.loadModule(id);
		if (m != null) {
			return cast(m.analyse());
		}

		return null;
	}
}