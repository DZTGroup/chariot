package com.aperture.docx.service;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.aperture.docx.Docx;
import com.aperture.docx.dom.DocxTreeStructure;
import com.aperture.docx.dom.ModuleCompiler;

public class DocxService {
	public static void parseDocument(String name, String path) throws Docx4JException{
		name = name.replaceAll("\\.docx$", "");
		Docx doc = new Docx(path);
		new DocxTreeStructure(doc).parseAs(name);
	}
	public static void getModule(String name) throws Docx4JException{
		ModuleCompiler mc = new ModuleCompiler();
		com.aperture.docx.dom.Module m = new com.aperture.docx.dom.Module();
		m.init("sample");
		mc.pendModule(m);
		mc.save(settings.Constant.DEBUG_PATH + "compiled.docx");
	}
}