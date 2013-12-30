package com.aperture.docx.dom;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

//import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments.Comment;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R.CommentReference;

import com.aperture.docx.Docx;

public class DocxTreeStructure implements TraversalUtil.Callback {
	Docx doc;

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
	List<Module> extractedModules = new ArrayList<Module>();

	// comment
	BigInteger maxCommentId = BigInteger.ZERO;

	// traversal facility
	Stack<Object> currentPath = new Stack<Object>();

	public DocxTreeStructure(Docx d) {
		doc = d;
	}

	protected Object getLastChild(Object parent) {
		List<Object> children = getChildren(parent);
		if (children != null) {
			return children.get(children.size() - 1);
		}

		return null;
	}

	// Depth first
	@Override
	public void walkJAXBElements(Object parent) {

		List<Object> children = getChildren(parent);
		if (children != null) {
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

	@Override
	public List<Object> apply(Object o) {
		// getAllElementFromObject(o);
		//
		if (o instanceof CommentRangeStart) {
			Module m = new Module();
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
				m.tail(commentRangeStarter,
						theirParent.get(o) != null ? theirParent.get(o)
								.getClass() : null);
			} catch (Docx4JException e) {
				//
				e.printStackTrace();
				errors.add(e);
			}
			moduleStack.push(m);

		} else if (o instanceof CommentRangeEnd) {
			currentPop = moduleStack.pop();
			// CommentRangeStart is just a child
			Object commentRangeEnder = XmlUtils.deepCopy(o);
			currentPop.tail(commentRangeEnder,
					theirParent.get(o) != null ? theirParent.get(o).getClass()
							: null);
		} else if (o instanceof CommentReference) {
			CommentReference cr = (CommentReference) o;
			BigInteger id = cr.getId();
			// update
			if (maxCommentId.compareTo(id) < 0)
				maxCommentId = id;

			for (Comment c : doc.getComment().getComment()) {
				if (id.equals(c.getId())) {
					// System.out.println(Docx.extractText(c));
					if (currentPop != null) {
						currentPop.id = Docx.extractText(c);
						currentPop.doc.createComment(id, currentPop.id);
						Object ref = Docx.createRunCommentReference(id);

						currentPop.tail(ref,
								theirParent.get(o) != null ? theirParent.get(o)
										.getClass() : null);

						extractedModules.add(currentPop);

						currentPop = null;
					}
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

				m.tail(oc, theirParent.get(o) != null ? theirParent.get(o)
						.getClass() : null);
			} else {
				Object parent = theirParent.get(o);
				if (parent != null && removeList.contains(parent)) {
					while (parent != null && removeList.contains(parent)) {
						parent = lastSibling.get(parent);
					}

					if (parent != null) {
						if (o instanceof org.docx4j.wml.Text)
							System.out.println(((org.docx4j.wml.Text) o)
									.getValue());
						moveMapping.put(o, parent);
						moveList.add(o);
						// ((ContentAccessor)parent).getContent().add(o);
					}
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
				m.doc.save(settings.Constant.MODULE_PATH + m.id + ".docx");
			}
			// also save itself as a module
			List<Object> docFlow = doc.getBody().getContent();
			BigInteger commentId = maxCommentId.add(BigInteger.ONE);
			
			doc.createComment(commentId, name);
			Object start = Docx.createCommentRangeStart(commentId);
			Object end = Docx.createCommentRangeEnd(commentId);
			Object ref = Docx.createRunCommentReference(commentId);
			
			docFlow.add(0, start);
			List<Object> tail = doc.getTailAccessor();
			tail.add(end);
			tail.add(ref);
			
			doc.save(settings.Constant.MODULE_PATH + name + ".docx");
		}
	}
}
