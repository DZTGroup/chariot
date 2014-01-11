package util;

import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.aperture.docx.core.Docx;
import com.aperture.docx.templating.Module;
import com.aperture.docx.templating.ModuleParser;
import com.aperture.docx.templating.ModuleCompiler;

public class DocxService {
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
	
	public static models.template.Module cast(Module.ModuleModel m){
		return new models.template.Module(m.name, m.text, m.children);
	}

	public static void parseDocument(DocType type, String name, String path)
			throws Docx4JException {
		name = name.replaceAll("\\.docx$", "");
		Docx doc = new Docx(path);
		new ModuleParser(doc).parseAs(name);

		if (type == DocType.DOC) {
			List<models.File> files = models.File.find.where().eq("name", name)
					.findList();
			// not found, insert
			if (files.size() == 0) {
				models.File file = new models.File(name, type.getType());
				file.save();
			}
		}
	}

	public static boolean getCompiledModule(String name) throws Docx4JException {
		ModuleCompiler mc = new ModuleCompiler();
		com.aperture.docx.templating.Module m = new com.aperture.docx.templating.Module();
		m.init(name);
		if (!m.isInitialized()) {
			return false;
		}
		mc.pendModule(m);
		mc.save(settings.Constant.USER_DIR + "/" + name + ".docx");

		return true;
	}

	public static models.template.Module analyzeModule(String name)
			throws Docx4JException {
		Module m = new Module();
		m.init(name);
		if (m.isInitialized()) {
			return cast(m.analyse());
		}

		return null;
	}
}