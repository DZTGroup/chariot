package com.aperture.docx.service;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.aperture.docx.Docx;
import com.aperture.docx.dom.DocxTreeStructure;

public class DocxService {
	public static void parseDocument(String name, String path) throws Docx4JException{
		name = name.replaceAll("\\.docx$", "");
		Docx doc = new Docx(path);
		new DocxTreeStructure(doc).parseAs(name);
	}
}
