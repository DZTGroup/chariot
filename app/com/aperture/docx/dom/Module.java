package com.aperture.docx.dom;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.R;
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

	boolean isQuestionMarker(Object start) {
		Object next = doc.getNextObject(start);
		if (next instanceof R) {
			if (Docx.extractText(next).matches("^__+$")) {
				Object end = doc.getNextObject(next);

				if (end instanceof CommentRangeEnd) {
					return true;
				}
			}
		}
		return false;
	}

	public BiMap<Module, Object> getSubModuleEntry() throws Docx4JException {
		BiMap<Module, Object> result = HashBiMap.create();
		for (Comment c : doc.getComment().getComment()) {
			BigInteger id = c.getId();

			Object cstart = doc.getCommentRangeStartById(id);
			if (!isQuestionMarker(cstart)) {
				ContentAccessor parent = (ContentAccessor) ((CommentRangeStart) cstart)
						.getParent();
				int index = parent.getContent().indexOf(cstart);

				if (index + 1 < parent.getContent().size()
						&& parent.getContent().get(index + 1) instanceof CommentRangeEnd) {

					Module newModule = new Module();
					newModule.init(Docx.extractText(c));

					if (newModule.isInitialized() == true) {
						result.put(newModule, cstart);
					}
				}
			}
		}

		return result;
	}

	class QuestionMarker {
		public String qid;
		public int index;
	}

	public models.template.Module analyse() {
		final List<Object> searchList = new ArrayList<Object>();
		final StringBuilder pureText = new StringBuilder();
		new TraversalUtil(doc.getBody(), new TraversalUtil.CallbackImpl() {

			@Override
			public List<Object> apply(Object o) {
				if (o instanceof org.docx4j.wml.Text) {
					pureText.append(((org.docx4j.wml.Text) o).getValue());
				}

				if (o instanceof CommentRangeStart) {
					if (isQuestionMarker(o)) {
						QuestionMarker qm = new QuestionMarker();
						qm.qid = doc.getCommentTextById(((CommentRangeStart) o)
								.getId());
						qm.index = pureText.length();
						searchList.add(qm);
					} else {
						String name = doc
								.getCommentTextById(((CommentRangeStart) o)
										.getId());
						if (name.equals(id))
							return null;
						Module sub = new Module();
						try {
							sub.init(name);
						} catch (Docx4JException e) {
							//
							e.printStackTrace();
						}
						if (sub.isInitialized() == true) {
							models.template.Module m = sub.analyse();
							searchList.add(m);
						}
					}
				}
				return null;
			}
		});
		List<Object> result = new ArrayList<Object>();
		for (Object o : searchList) {
			if (o instanceof models.template.Module)
				result.add(o);
			if (o instanceof QuestionMarker) {
				QuestionMarker qm = (QuestionMarker) o;
				// bound checking
				int start = qm.index
						- settings.Constant.QUESTION_CONTEXT_RADIUS;
				start = start > 0 ? start : 0;

				int end = qm.index + settings.Constant.QUESTION_CONTEXT_RADIUS
						+ 4;
				end = end <= pureText.length() ? end : pureText.length();

				models.template.Question q = new models.template.Question(
						qm.qid, pureText.substring(start, end));

				result.add(q);
			}
		}
		return new models.template.Module(id, Docx.extractText(doc.getBody()),
				result);
	}
}
