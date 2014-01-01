package com.aperture.docx.service;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.aperture.docx.Docx;
import com.aperture.docx.dom.DocxTreeStructure;
import com.aperture.docx.dom.Module;
import com.aperture.docx.dom.ModuleCompiler;

public class DocxService {
	public static void parseDocument(String name, String path)
			throws Docx4JException {
		name = name.replaceAll("\\.docx$", "");
		Docx doc = new Docx(path);
		new DocxTreeStructure(doc).parseAs(name);
	}

	public static boolean getCompiledModule(String name) throws Docx4JException {
		ModuleCompiler mc = new ModuleCompiler();
		com.aperture.docx.dom.Module m = new com.aperture.docx.dom.Module();
		m.init(name);
		if (!m.isInitialized()) {
			return false;
		}
		mc.pendModule(m);
		mc.save(settings.Constant.USER_DIR + "/" + name + ".docx");

		return true;
	}
	
	public static models.template.Module analyzeModule(String name) throws Docx4JException{
		Module m = new Module();
		m.init(name);
		if(m.isInitialized()){
			return m.analyse();
		}
		
		return null;
	}
}