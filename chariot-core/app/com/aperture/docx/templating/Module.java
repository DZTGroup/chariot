package com.aperture.docx.templating;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments.Comment;
import org.docx4j.wml.ContentAccessor;

import play.Logger;

import settings.Constant;

import com.aperture.docx.core.BinaryLoader;
import com.aperture.docx.core.BinarySaver;
import com.aperture.docx.core.Docx;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class Module {
	long id = -1;
	String moduleName;
	
	// get id
	public long getId(){
		return id;
	}
	
	// use it as a tag for cache
	String updateTag;

	Docx doc;
	BinarySaver saver;

	boolean initialized = false;

	public boolean isInitialized() {
		return initialized;
	}

	// use this init only in ModuleParser
	void init(Object head) throws Docx4JException {
		doc = new Docx();

		if (head != null) {
			doc.getBody().getContent().add(head);
			// theirParent.put(head, doc.getBody());
		}

		initialized = true;
	}

	// set this if you need save
	public void setSaver(BinarySaver s) {
		this.saver = s;
	}
	public void setName(String name) {
		moduleName = name;
		if (this.saver != null) {
			this.saver.setName(name);
		}
	}

	public void save() throws Docx4JException {
		this.doc.save(saver);
	}

	// saver not needed if init this way
	public void init(BinaryLoader loader) throws Docx4JException {
		doc = new Docx(loader);
		this.id = loader.getId();
		this.moduleName = loader.getName();
		this.updateTag  = loader.getUpdateTag();

		initialized = true;
	}

	public String getName() {
		return this.moduleName;
	}
	
	public String getUpdateTag() {
		return this.updateTag;
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

					Module newModule = ModuleIO.loadModule(Docx.extractText(c));

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
		public long id;
		public String name;
		public String text;

		public List<Object> children;

		public ModuleModel(long i, String n, String t, List<Object> c) {
			id = i;
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
						if (name.equals(moduleName))
							return null;
						try {
							Module sub = ModuleIO.loadModule(name);
							if (sub != null && sub.isInitialized() == true) {
								ModuleModel m = sub.analyse();
								searchList.add(m);
							}
						} catch (Docx4JException e) {
							//
							Logger.error("Parsing Module " + name + " error:",
									e);
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
		return new ModuleModel(this.id, moduleName, Docx.extractText(doc.getBody()),
				result);
	}
}
