package com.aperture.docx.templating;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Comments.Comment;
import org.docx4j.wml.R.CommentReference;
import org.jvnet.jaxb2_commons.ppp.Child;

import com.aperture.docx.core.Docx;
import com.google.common.collect.BiMap;

public class ModuleCompiler {
	Docx doc;

	int moduleCount = 0;

	public ModuleCompiler() throws Docx4JException {
		doc = new Docx();
	}

	public void save(String path) throws Docx4JException {
		doc.save(path);
	}

	private void pend(Object o) {
		// normal;
		// if not inside module content, ignore
		Object oc = XmlUtils.deepCopy(o);
		// try
		if (oc instanceof ContentAccessor)
			((ContentAccessor) oc).getContent().clear();

		doc.tail(oc, ((Child) o).getParent() != null ? ((Child) o).getParent()
				.getClass() : null);
	}
	
	public static interface PendRequirement{
		boolean apply(Module m);
	}
	
	// default
	public void pendModule(Module m)throws Docx4JException {
		this.pendModule(m, new PendRequirement(){
			// default impl, simple
			public boolean apply(Module m){
				return true;
			}
		});
	}

	public void pendModule(Module m, final PendRequirement requirement) throws Docx4JException {
		if (m == null || !m.initialized)
			return;
		
		// check if the module meets the requirement
		if ( ! requirement.apply(m) )
			return;
		
		// if moduleCount == 0, its the root module
		final boolean isRootModule = moduleCount == 0;
		moduleCount++;

		final Module pendingModule = m;
		final List<Docx4JException> errors = new ArrayList<Docx4JException>();

		final BiMap<Module, Object> subs = m.getSubModuleEntry();
		new TraversalUtil(pendingModule.doc.getBody(),
				new TraversalUtil.CallbackImpl() {
					BigInteger wrapperId = null;
					boolean isInsideModule = false;

					@Override
					public List<Object> apply(Object o) {
						if (o instanceof CommentRangeStart) {
							if (wrapperId == null) {
								// this is the wrapper of this module
								wrapperId = ((CommentRangeStart) o).getId();
								isInsideModule = true;
							} else {
								// sub module
								pend(o);
								Module sub = subs.inverse().get(o);
								try {
									// continue with predict
									pendModule(sub, requirement);
								} catch (Docx4JException e) {
									//
									e.printStackTrace();
									errors.add(e);
								}
							}
						} else if (o instanceof CommentRangeEnd) {
							// just pend unless its module ending
							if (!((CommentRangeEnd) o).getId()
									.equals(wrapperId)) {
								pend(o);
								// clear
							} else {
								isInsideModule = false;
							}

						} else if (o instanceof CommentReference) {
							// just pend unless its module ending
							// also, add comment to doc comment parts
							CommentReference cr = (CommentReference) o;
							BigInteger id = cr.getId();

							if (!id.equals(wrapperId)) {
								// just pend
								for (Comment c : pendingModule.doc.getComment()
										.getComment()) {
									if (id.equals(c.getId())) {
										String text = Docx.extractText(c);
										doc.createComment(id, text);
										Object ref = Docx
												.createCommentReference(id);

										doc.tail(ref,
												cr.getParent() != null ? cr
														.getParent().getClass()
														: null);

										break;
									}
								}
							}
						} else {
							if ((isRootModule && wrapperId == null)
									|| (wrapperId != null && isInsideModule)) {
								pend(o);
							}
						}

						//
						return null;
					}
				});
	}
	
	public void detemplate(Map<String, String> answers){
		QuestionUtil.fillQuestionBlank(this.doc, answers);
		this.doc.removeAllComments();
	}
	
	public String convertToPdf(String name) throws Docx4JException {
		return doc.convertToPdf(name);
	}
	
	public String convertToPdf(String name, String path) throws Docx4JException {
		return doc.convertToPdf(name, path);
	}
}
