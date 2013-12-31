package com.aperture.docx.dom;

import java.io.File;
import java.math.BigInteger;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments.Comment;
import org.docx4j.wml.ContentAccessor;

import com.aperture.docx.Docx;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class Module {
	String id;

	Docx doc;

	boolean initialized = false;

	public boolean isInitialized() {
		return initialized;
	}

	public void init(Object head) throws Docx4JException {
		doc = new Docx();

		if (head != null) {
			doc.getBody().getContent().add(head);
			// theirParent.put(head, doc.getBody());
		}

		initialized = true;
	}

	public void init(String name) throws Docx4JException {
		String path = settings.Constant.MODULE_PATH + "/" + name + ".docx";
		File f = new File(path);
		if (f.isFile() && f.canRead()) {
			doc = new Docx(path);
			this.id = name;

			initialized = true;
		}
	}

	public BiMap<Module, Object> getSubModuleEntry() throws Docx4JException {
		BiMap<Module, Object> result = HashBiMap.create();
		for (Comment c : doc.getComment().getComment()) {
			BigInteger id = c.getId();

			Object cstart = doc.getCommentRangeStartById(id);
			ContentAccessor parent = (ContentAccessor) ((CommentRangeStart) cstart)
					.getParent();
			int index = parent.getContent().indexOf(cstart);

			if (index + 1 < parent.getContent().size()
					&& parent.getContent().get(index + 1) instanceof CommentRangeEnd) {

				Module newModule = new Module();
				newModule.init(Docx.extractText(c));

				if (newModule.initialized == true) {
					result.put(newModule, cstart);
				}
			}
		}

		return result;
	}
}
