package com.aperture.docx.templating;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.aperture.docx.core.*;

// scala code for mongo gridfs io
import com.aperture.docx.scala.Gfs;

public class ModuleIO implements BinaryLoader, BinarySaver {
	models.Module module;

	protected ModuleIO() {
	}

	public static Module loadModule(String name) throws Docx4JException {
		Module m = null;

		ModuleIO io = new ModuleIO();
		io.module = models.Module.find.select("id, name, gfsId").where()
				.eq("name", name).findUnique();
		if (io.module != null) {
			m = new Module();

			m.init(io);
		}

		return m;
	}

	public static Module loadModule(long id) throws Docx4JException {
		Module m = null;

		ModuleIO io = new ModuleIO();
		io.module = models.Module.find.select("id, name, gfsId").where()
				.idEq(Long.valueOf(id)).findUnique();
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

	public static long newDocument(String name, String path)
			throws Docx4JException {
		ModuleIO io = new ModuleIO();

		io.module = new models.Module();
		io.module.name = name;

		new ModuleParser(new Docx(path), io).parseAs(name);
		return io.module.id;
	}

	@Override
	public void saveAsBinaryData(byte[] data) {
		if (module.name != null) {
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			String gfsId = Gfs.save(module.name, in);
			
			models.Module m = models.Module.find.where()
					.eq("name", module.name).findUnique();
			if (m != null) {
				module.id = m.id;
				module.gfsId = gfsId;
				module.update();
			} else {
				module.gfsId = gfsId;
				module.save();
			}
		}
	}

	@Override
	public byte[] loadAsBinaryData() {
		//
		if ( module.gfsId != null ){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			Gfs.load(module.gfsId, out);
		
			return out.toByteArray();
		}
		return null;
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
	
	@Override
	public String getUpdateTag() {
		//
		return module.gfsId;
	}

	@Override
	public long getId() {
		//
		return module.id;
	}
}
