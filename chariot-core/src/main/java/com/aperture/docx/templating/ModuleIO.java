package com.aperture.docx.templating;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.aperture.docx.core.*;

public class ModuleIO implements BinaryLoader, BinarySaver {
	models.Module module;

	protected ModuleIO() {
	}

	public static Module loadModule(long id) throws Docx4JException {
		Module m = null;

		ModuleIO io = new ModuleIO();
		io.module = models.Module.find.byId(id);
		if (io.module != null) {
			m = new Module();

			m.init(io);
		}

		return m;
	}

	public static Module newModuleWithSaver() {
		ModuleIO io = new ModuleIO();

		io.module = new models.Module();

		Module m = new Module();
		m.setSaver(io);
		return m;
	}

	public static void newDocument(String name, String path)
			throws Docx4JException {
		ModuleIO io = new ModuleIO();

		io.module = new models.Module();
		io.module.name = name;

		new ModuleParser(new Docx(path), io).parseAs(name);
	}

	@Override
	public void saveAsBinaryData(byte[] data) {
		//
		module.content = data;
		module.save();
	}

	@Override
	public byte[] loadAsBinaryData() {
		//
		return module.content;
	}

	@Override
	public String getName() {
		//
		return module.name;
	}

	@Override
	public void setName(String name) {
		//
		module.name = name;
	}
}
