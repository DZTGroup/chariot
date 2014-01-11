package com.aperture.docx.templating;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments.Comment;
import org.docx4j.wml.ContentAccessor;

import settings.Constant;

import com.aperture.docx.core.Docx;
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
			if (!QuestionUtil.isQuestionMarker(doc, cstart)) {
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

	private class QuestionMarker {
		String qid;
		int start;
		int end;
	}

	public static class QuestionModel {
		public String questionId;
		public String context;

		public QuestionModel(String id, String c) {
			questionId = id;
			context = c;
		}
	}

	public static class ModuleModel {
		public String name;
		public String text;

		public List<Object> children;

		public ModuleModel(String n, String t, List<Object> c) {
			name = n;
			text = t;
			children = c;
		}
	}

	public ModuleModel analyse() {
		final List<Object> searchList = new ArrayList<Object>();
		final StringBuilder pureText = new StringBuilder();
		new TraversalUtil(doc.getBody(), new TraversalUtil.CallbackImpl() {

			@Override
			public List<Object> apply(Object o) {
				if (o instanceof org.docx4j.wml.Text) {
					pureText.append(((org.docx4j.wml.Text) o).getValue());
				}

				if (o instanceof CommentRangeStart) {
					int blankSize = QuestionUtil.getQuestionBlankSize(doc, o);
					if (blankSize > 0) {
						QuestionMarker qm = new QuestionMarker();
						qm.qid = doc.getCommentTextById(((CommentRangeStart) o)
								.getId());
						qm.start = pureText.length();
						qm.end = qm.start + blankSize;
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
							ModuleModel m = sub.analyse();
							searchList.add(m);
						}
					}
				}
				return null;
			}
		});
		List<Object> result = new ArrayList<Object>();
		for (Object o : searchList) {
			if (o instanceof ModuleModel)
				result.add(o);
			if (o instanceof QuestionMarker) {
				QuestionMarker qm = (QuestionMarker) o;
				// bound checking
				int start = qm.start
						- settings.Constant.QUESTION_CONTEXT_RADIUS;
				start = start > 0 ? start : 0;

				int end = qm.end + settings.Constant.QUESTION_CONTEXT_RADIUS;
				end = end <= pureText.length() ? end : pureText.length();

				QuestionModel q = new QuestionModel(qm.qid, pureText.substring(
						start, qm.start)
						+ Constant.QUESTON_BLANK_REPRESENTATION
						+ pureText.substring(qm.end, end));

				result.add(q);
			}
		}
		return new ModuleModel(id, Docx.extractText(doc.getBody()), result);
	}
}
