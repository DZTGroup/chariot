package com.aperture.docx.core;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.io3.Save;

import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.R.CommentReference;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Comments.Comment;
import org.docx4j.wml.ContentAccessor;
import org.jvnet.jaxb2_commons.ppp.Child;

import play.Logger;

public class Docx {
	// debug mark
	public boolean _debug = false;

	// maintain docx structure here
	Map<Object, Object> theirParent = new HashMap<Object, Object>();

	WordprocessingMLPackage wordMLPackage;
	org.docx4j.wml.Document wmlDocumentEl;

	public static org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

	public Docx(String path) throws Docx4JException {
		wordMLPackage = WordprocessingMLPackage.load(new java.io.File(path));
		wmlDocumentEl = (org.docx4j.wml.Document) wordMLPackage
				.getMainDocumentPart().getJaxbElement();
	}

	public Docx(BinaryLoader loader) throws Docx4JException {
		ByteArrayInputStream is = new ByteArrayInputStream(
				loader.loadAsBinaryData());
		wordMLPackage = WordprocessingMLPackage.load(is);
		wmlDocumentEl = (org.docx4j.wml.Document) wordMLPackage
				.getMainDocumentPart().getJaxbElement();
	}

	public Docx() throws Docx4JException {
		// open template
		wordMLPackage = WordprocessingMLPackage.load(new java.io.File(
				settings.Constant.DOCX_TEMPLATE));
		wmlDocumentEl = (org.docx4j.wml.Document) wordMLPackage
				.getMainDocumentPart().getJaxbElement();

		// clear content ( use style only )
		wmlDocumentEl.getBody().getContent().clear();

		// insert comment part
		CommentsPart cp = new CommentsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(cp);
		Comments comments = factory.createComments();
		cp.setJaxbElement(comments);
	}

	/*
	 * private void parseStructure() { new
	 * TraversalUtil(wmlDocumentEl.getBody(), new TraversalUtil.Callback() {
	 * 
	 * @Override public void walkJAXBElements(Object parent) { List<Object>
	 * children = getChildren(parent); if (children != null) { for (Object o :
	 * children) { // if its wrapped in javax.xml.bind.JAXBElement, // get its
	 * // value; this is ok, provided the results of // the Callback // won't be
	 * marshalled o = XmlUtils.unwrap(o);
	 * 
	 * theirParent.put(o, parent);
	 * 
	 * this.apply(o);
	 * 
	 * if (this.shouldTraverse(o)) { walkJAXBElements(o); } } } }
	 * 
	 * @Override public List<Object> getChildren(Object o) { return
	 * TraversalUtil.getChildrenImpl(o); }
	 * 
	 * @Override public boolean shouldTraverse(Object o) { return true; }
	 * 
	 * @Override public List<Object> apply(Object arg0) { // TODO Auto-generated
	 * method stub return null; } }); } public Object getParentFor(Object o) {
	 * // parse it when needed parseStructure(); return theirParent.get(o); }
	 */

	public org.docx4j.wml.Body getBody() {
		return wmlDocumentEl.getBody();
	}

	public Comments getComment() {
		return wordMLPackage.getMainDocumentPart().getCommentsPart()
				.getContents();
	}

	public String getCommentTextById(BigInteger id) {
		for (Comment c : this.getComment().getComment()) {
			if (c.getId().equals(id))
				return Docx.extractText(c);
		}

		return null;
	}

	public Object getCommentRangeStartById(BigInteger id) {
		final BigInteger thisId = id;
		final List<Object> container = new ArrayList<Object>();
		new TraversalUtil(this.getBody(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				//
				if (o instanceof CommentRangeStart) {
					if (((CommentRangeStart) o).getId().equals(thisId))
						container.add(o);
				}
				return null;
			}
		});
		return container.size() > 0 ? container.get(0) : null;
	}

	// looks cool
	public void tail(Object o, Class<?> containerType) {
		if (containerType == null) {
			return;
		}

		List<Object> siblings = Docx.getNodesByClass(this.getBody(),
				containerType);

		if (siblings.size() > 0) {
			ContentAccessor ca = (ContentAccessor) siblings
					.get(siblings.size() - 1);
			ca.getContent().add(o);
		} else {
			this.getBody().getContent().add(o);
		}
	}

	public List<Object> getTailAccessor() {
		final List<Object> visited = new ArrayList<Object>();
		new TraversalUtil(this.getBody(), new TraversalUtil.Callback() {
			@Override
			public void walkJAXBElements(Object parent) {
				List<Object> children = getChildren(parent);
				if (children != null && children.size() > 0) {
					Object o = children.get(children.size() - 1);
					o = XmlUtils.unwrap(o);

					this.apply(o);

					if (this.shouldTraverse(o)) {
						walkJAXBElements(o);
					}
				}
			}

			@Override
			public List<Object> getChildren(Object o) {
				//
				return TraversalUtil.getChildrenImpl(o);
			}

			@Override
			public boolean shouldTraverse(Object o) {
				if (o instanceof org.docx4j.wml.Br
					|| o instanceof org.docx4j.wml.R.Tab
					|| o instanceof org.docx4j.wml.R.LastRenderedPageBreak) {
					return false;
				}
				return true;
			}

			@Override
			public List<Object> apply(Object o) {
				if (o instanceof ContentAccessor)
					visited.add(o);
				return null;
			}
		});

		if (visited.size() > 0) {
			Logger.info(visited.get(visited.size() - 1).getClass().getName());
			return ((ContentAccessor) visited.get(visited.size() - 1))
					.getContent();
		}
		return this.getBody().getContent();
	}

	public void save(String path) throws Docx4JException {
		// wordMLPackage.save(new java.io.File(path));

		Save saver = new Save(wordMLPackage);
		try {
			saver.save(new FileOutputStream(new java.io.File(path)));
		} catch (FileNotFoundException e) {
			//
			Logger.error(e.getMessage());
		}
	}

	// use this to store to database
	public void save(com.aperture.docx.core.BinarySaver s)
			throws Docx4JException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Save saver = new Save(wordMLPackage);
		saver.save(os);

		// call interface
		s.saveAsBinaryData(os.toByteArray());
	}

	// comments related
	public void createComment(java.math.BigInteger commentId, String message) {

		org.docx4j.wml.Comments.Comment comment = factory
				.createCommentsComment();
		comment.setId(commentId);
		comment.setAuthor(settings.Constant.SYSTEM_AUTHOR);
		org.docx4j.wml.P commentP = factory.createP();
		comment.getContent().add(commentP);
		org.docx4j.wml.R commentR = factory.createR();
		commentP.getContent().add(commentR);
		org.docx4j.wml.Text commentText = factory.createText();
		commentR.getContent().add(commentText);

		commentText.setValue(message);

		this.getComment().getComment().add(comment);
	}

	public static org.docx4j.wml.R createRunCommentReference(
			java.math.BigInteger commentId) {

		org.docx4j.wml.R run = factory.createR();
		org.docx4j.wml.R.CommentReference commentRef = factory
				.createRCommentReference();
		run.getContent().add(commentRef);
		commentRef.setId(commentId);

		return run;
	}

	public static org.docx4j.wml.R.CommentReference createCommentReference(
			java.math.BigInteger commentId) {

		org.docx4j.wml.R.CommentReference commentRef = factory
				.createRCommentReference();
		commentRef.setId(commentId);
		return commentRef;

	}

	public static org.docx4j.wml.CommentRangeStart createCommentRangeStart(
			java.math.BigInteger commentId) {
		org.docx4j.wml.CommentRangeStart rs = factory.createCommentRangeStart();
		rs.setId(commentId);
		return rs;
	}

	public static org.docx4j.wml.CommentRangeEnd createCommentRangeEnd(
			java.math.BigInteger commentId) {
		org.docx4j.wml.CommentRangeEnd rs = factory.createCommentRangeEnd();
		rs.setId(commentId);
		return rs;
	}

	public static String extractText(Object part) {
		final StringBuilder sb = new StringBuilder();
		new TraversalUtil(part, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof org.docx4j.wml.Text)
					sb.append(((org.docx4j.wml.Text) o).getValue());
				return null;
			}
		});
		return sb.toString();
	}

	public Object getNextObject(Object o) {
		ContentAccessor parent = (ContentAccessor) ((Child) o).getParent();

		int index = parent.getContent().indexOf(o) + 1;
		if (index < parent.getContent().size()) {
			return parent.getContent().get(index);
		}

		return null;
	}

	public static List<Object> getNodesByClass(Object part, Class<?> type) {
		final List<Object> parts = new ArrayList<Object>();
		final Class<?> toSearch = type;
		new TraversalUtil(part, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof JAXBElement)
					o = ((JAXBElement<?>) o).getValue();
				if (o.getClass().equals(toSearch)) {
					parts.add(o);
				}
				return null;
			}
		});
		return parts;
	}
	
	// util
	private static boolean remove(List<Object> theList, Object bm)  {
	    // Can't just remove the object from the parent,
	    // since in the parent, it may be wrapped in a JAXBElement
	    for (Object ox : theList) {
	        if (XmlUtils.unwrap(ox).equals(bm)) {
	            return theList.remove(ox);
	        }
	    }
	    return false;
	}
	
	private static class CommentFinder extends TraversalUtil.CallbackImpl {

		List<Child> commentElements = new ArrayList<Child>();

		@Override
		public List<Object> apply(Object o) {
			if (o instanceof javax.xml.bind.JAXBElement
				&& (((JAXBElement)o).getName().getLocalPart().equals("commentReference")
					|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeStart")
						|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeEnd")                                      
			)) {
				commentElements.add( (Child)XmlUtils.unwrap(o) );
			} else 
				if (o instanceof CommentReference || 
					o instanceof CommentRangeStart || 
			o instanceof CommentRangeEnd) {
				commentElements.add((Child)o);
			}
			return null;
		}

		@Override // to setParent
		public void walkJAXBElements(Object parent) {

			List children = getChildren(parent);
			if (children != null) {

				for (Object o : children) {

					if (o instanceof javax.xml.bind.JAXBElement
						&& (((JAXBElement)o).getName().getLocalPart().equals("commentReference")
							|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeStart")
								|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeEnd")                                      
					)) {

						((Child)((JAXBElement)o).getValue()).setParent(XmlUtils.unwrap(parent));
					} else {                        
						o = XmlUtils.unwrap(o);
						if (o instanceof Child) {
							((Child)o).setParent(XmlUtils.unwrap(parent));
						}
					}


					this.apply(o);

					if (this.shouldTraverse(o)) {
						walkJAXBElements(o);
					}

				}
			}
		}           
	}
	
	
	public void removeAllComments(){
		CommentFinder cf = new CommentFinder();
		new TraversalUtil(this.getBody(), cf);

		for (Child commentElement : cf.commentElements) {
			Object parent = commentElement.getParent();
			remove(((ContentAccessor)parent).getContent(), commentElement );
		}
	}

	public static void removeUncontained(Object part,
			Collection<Object> whiteList) {
		final Collection<Object> list = whiteList;
		final List<Object> toRemove = new ArrayList<Object>();
		final Map<Object, Object> theirParent = new HashMap<Object, Object>();

		new TraversalUtil(part, new TraversalUtil.Callback() {
			@Override
			public List<Object> apply(Object o) {
				if (!list.contains(o)) {
					toRemove.add(o);
				}
				return null;
			}

			@Override
			public List<Object> getChildren(Object o) {
				//
				return TraversalUtil.getChildrenImpl(o);
			}

			@Override
			public boolean shouldTraverse(Object o) {
				if (o instanceof org.docx4j.wml.Br
					|| o instanceof org.docx4j.wml.R.Tab
					|| o instanceof org.docx4j.wml.R.LastRenderedPageBreak) {
					return false;
				}
				return true;
			}

			@Override
			public void walkJAXBElements(Object parent) {
				List<Object> children = getChildren(parent);
				if (children != null) {
					for (Object o : children) {
						// if its wrapped in javax.xml.bind.JAXBElement, get its
						// value; this is ok, provided the results of the
						// Callback
						// won't be marshalled
						o = XmlUtils.unwrap(o);

						theirParent.put(o, parent);

						this.apply(o);

						if (this.shouldTraverse(o)) {
							walkJAXBElements(o);
						}
					}
				}
			}
		});

		for (Object o : toRemove) {
			ContentAccessor parent = (ContentAccessor) theirParent.get(o);
			if (parent != null) {
				parent.getContent().remove(o);
			}
		}
	}
	
	public String convertToPdf(String name, String path) throws Docx4JException {
		// check system supporting
		if (settings.Constant.LIBRE_OFFICE == null){
			Logger.error("Application running on OS that is not supported.");
			
			return null;
		}
		
		String docPath = settings.Constant.USER_DIR + "/" + name + ".docx";
		this.save(docPath);
		
		try{
			ProcessBuilder pb = new ProcessBuilder(settings.Constant.LIBRE_OFFICE, 
				"--headless", 
				"--convert-to", "pdf",
				"--outdir", path,
				docPath);
			pb.environment().put("HOME", settings.Constant.USER_DIR);
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while((line = reader.readLine()) != null){
				Logger.info(line);
			}
		
			return path + "/" + name + ".pdf";
		} catch (java.io.IOException e){
			Logger.error(e.getMessage());
			
			return null;
		}
	}
	
	public String convertToPdf(String name) throws Docx4JException {
		// call
		return convertToPdf(name, settings.Constant.USER_DIR);
	}
}
