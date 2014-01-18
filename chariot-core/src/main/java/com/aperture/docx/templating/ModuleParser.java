package com.aperture.docx.templating;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

//import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments.Comment;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;
import org.docx4j.wml.R.CommentReference;

import com.aperture.docx.core.BinarySaver;
import com.aperture.docx.core.Docx;

public class ModuleParser implements TraversalUtil.Callback {
	Docx doc;
	BinarySaver saver;

	List<Exception> errors = new ArrayList<Exception>();

	// structure
	Map<Object, Object> theirParent = new HashMap<Object, Object>();
	Map<Object, Object> lastSibling = new HashMap<Object, Object>();

	// transformation
	Map<Object, Object> moveMapping = new HashMap<Object, Object>();
	List<Object> moveList = new ArrayList<Object>();
	List<Object> removeList = new ArrayList<Object>();

	// modules
	Stack<Module> moduleStack = new Stack<Module>();
	Module currentPop = null;
	// result storage
	List<Module> extractedModules = new ArrayList<Module>();

	// comment
	BigInteger maxCommentId = BigInteger.ZERO;

	// traversal facility
	Stack<Object> currentPath = new Stack<Object>();

	// is now dealing with question?
	boolean isDealingQuestion = false;
	Map<Object, Long> questionLocation = new HashMap<Object, Long>();

	/*
	 * Constructor!
	 */
	public ModuleParser(Docx d, BinarySaver s) {
		doc = d;
		saver = s;
	}

	protected Object getLastChild(Object parent) {
		List<Object> children = getChildren(parent);
		if (children != null && children.size() > 0) {
			return children.get(children.size() - 1);
		}

		return null;
	}

	// Depth first
	@Override
	public void walkJAXBElements(Object parent) {
		List<Object> children = getChildren(parent);
		if (children != null) {
			// overall path
			currentPath.push(parent);
			Object lastRef = null;
			for (Object o : children) {
				// if its wrapped in javax.xml.bind.JAXBElement, get its
				// value; this is ok, provided the results of the Callback
				// won't be marshalled
				o = XmlUtils.unwrap(o);

				theirParent.put(o, parent);

				if (lastRef != null) {
					lastSibling.put(o, lastRef);
				} else {
					Object lastParent = lastSibling.get(parent);
					if (lastParent != null) {
						Object lastOfLastParent = getLastChild(lastParent);
						if (lastOfLastParent != null) {
							lastSibling.put(o, lastOfLastParent);
						}
					}
				}
				lastRef = o;

				this.apply(o);

				if (this.shouldTraverse(o)) {
					walkJAXBElements(o);
				}
			}
			currentPath.pop();
		}
	}

	@Override
	public List<Object> getChildren(Object o) {
		return TraversalUtil.getChildrenImpl(o);
	}

	/**
	 * Decide whether this node's children should be traversed.
	 * 
	 * @return whether the children of this node should be visited
	 */
	@Override
	public boolean shouldTraverse(Object o) {
		return true;
	}

	// parsing
	// MAGIC, do not touch
	@Override
	public List<Object> apply(Object o) {
		// check if question here
		// if is question, ignore all control Comment(write them down just as
		// normal nodes)
		if (o instanceof CommentRangeStart && QuestionUtil.isQuestionMarker(doc, o)) {
			isDealingQuestion = true;
		}
		// pre check
		boolean isModuleStackEmptyBefore = moduleStack.isEmpty();
		if (o instanceof CommentRangeStart && !isDealingQuestion) {
			Module m = ModuleIO.newModuleWithSaver();
			List<Object> path = new ArrayList<Object>(currentPath);
			// ignore the first one of path, which is 'Body'
			Object head = null;
			if (path.size() > 1) {
				head = XmlUtils.deepCopy(path.get(1));
				Docx.removeUncontained(head, path);
			}
			try {
				m.init(head);
				// CommentRangeStart is just a child
				Object commentRangeStarter = XmlUtils.deepCopy(o);
				m.doc.tail(commentRangeStarter,
						theirParent.get(o) != null ? theirParent.get(o)
								.getClass() : null);

				// also mark it in parent module as a ref
				// if exist
				if (!moduleStack.isEmpty()) {
					removeList.add(o);
					commentRangeStarter = XmlUtils.deepCopy(o);
					moduleStack.peek().doc.tail(commentRangeStarter,
							theirParent.get(o) != null ? theirParent.get(o)
									.getClass() : null);
				}
			} catch (Docx4JException e) {
				//
				e.printStackTrace();
				errors.add(e);
			}
			moduleStack.push(m);
		} else if (o instanceof CommentRangeEnd && !isDealingQuestion) {
			currentPop = moduleStack.pop();
			// CommentRangeStart is just a child
			Object commentRangeEnder = XmlUtils.deepCopy(o);

			currentPop.doc.tail(commentRangeEnder,
					theirParent.get(o) != null ? theirParent.get(o).getClass()
							: null);
			// also mark it in parent module as a ref
			// if exist
			if (!moduleStack.isEmpty()) {
				removeList.add(o);
				commentRangeEnder = XmlUtils.deepCopy(o);
				moduleStack.peek().doc.tail(commentRangeEnder, theirParent
						.get(o) != null ? theirParent.get(o).getClass() : null);
			}

		} else if (o instanceof CommentReference && !isDealingQuestion) {
			CommentReference cr = (CommentReference) o;
			BigInteger id = cr.getId();

			// update
			if (maxCommentId.compareTo(id) < 0)
				maxCommentId = id;

			for (Comment c : doc.getComment().getComment()) {
				if (id.equals(c.getId())) {
					if (currentPop != null) {
						currentPop.setName(Docx.extractText(c));
						currentPop.doc.createComment(id, currentPop.moduleName);
						Object ref = Docx.createRunCommentReference(id);

						R parentR = (R) theirParent.get(o);
						currentPop.doc.tail(ref,
								theirParent.get(parentR) != null ? theirParent
										.get(parentR).getClass() : null);

						// also mark it in parent module as a ref
						// if exist
						if (!moduleStack.isEmpty()) {
							doc.getComment().getComment().remove(c);
							moduleStack.peek().setName(currentPop.moduleName);
							moduleStack.peek().doc.createComment(id,
									currentPop.moduleName);
							R.CommentReference refc = Docx
									.createCommentReference(id);
							// R already added, just add ref in this case
							moduleStack.peek().doc.tail(refc, R.class);
						}

						extractedModules.add(currentPop);

						currentPop = null;
					}
					break;
				}
			}
		} else {
			if (!moduleStack.isEmpty()) {
				removeList.add(o);

				Module m = moduleStack.peek();

				Object oc = XmlUtils.deepCopy(o);
				// try
				if (oc instanceof ContentAccessor)
					((ContentAccessor) oc).getContent().clear();

				m.doc.tail(oc, theirParent.get(o) != null ? theirParent.get(o)
						.getClass() : null);
			}

			// dealing question, mode CommentRef, generate question id
			if (o instanceof CommentReference && isDealingQuestion) {
				isDealingQuestion = false;
				CommentReference cr = (CommentReference) o;
				BigInteger id = cr.getId();

				// update
				if (maxCommentId.compareTo(id) < 0)
					maxCommentId = id;

				for (Comment c : doc.getComment().getComment()) {
					if (id.equals(c.getId())) {
						// remove comment from main
						doc.getComment().getComment().remove(c);
						// generate question id
						UUID qid = UUID.randomUUID();

						// create new for module ( or main )
						if (moduleStack.isEmpty()) {
							doc.createComment(id, qid.toString());
						} else {
							moduleStack.peek().doc.createComment(id,
									qid.toString());
						}
						break;
					}
				}
			}
		}

		if (isModuleStackEmptyBefore || moduleStack.isEmpty()) {
			Object parent = theirParent.get(o);
			if (parent != null && removeList.contains(parent)) {
				while (parent != null && removeList.contains(parent)) {
					parent = lastSibling.get(parent);
				}

				if (parent != null) {
					moveMapping.put(o, parent);
					moveList.add(o);
					// ((ContentAccessor)parent).getContent().add(o);
				}
			}
		}

		return null;
	}

	public void parseAs(String name) throws Docx4JException {
		new TraversalUtil(doc.getBody(), this);

		for (Object o : removeList) {
			ContentAccessor parent = (ContentAccessor) theirParent.get(o);
			if (parent != null) {
				parent.getContent().remove(o);
			}
		}

		for (Object o : moveList) {
			if (moveMapping.containsKey(o))
				((ContentAccessor) moveMapping.get(o)).getContent().add(o);
		}

		// check error
		if (errors.size() == 0) {
			// done, save
			for (Module m : extractedModules) {
				m.save();
			}
			// also save itself as a module
			List<Object> docFlow = doc.getBody().getContent();
			BigInteger commentId = maxCommentId.add(BigInteger.ONE);

			doc.createComment(commentId, name);
			Object start = Docx.createCommentRangeStart(commentId);
			Object end = Docx.createCommentRangeEnd(commentId);
			Object ref = Docx.createRunCommentReference(commentId);

			docFlow.add(0, start);
			// List<Object> tail = doc.getTailAccessor();
			// simply use the last paragraph
			ContentAccessor last = (ContentAccessor) docFlow
					.get(docFlow.size() - 1);
			last.getContent().add(end);
			last.getContent().add(ref);

			doc.save(this.saver);
		}
	}
}
