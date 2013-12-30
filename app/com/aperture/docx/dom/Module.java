package com.aperture.docx.dom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.wml.ContentAccessor;

import com.aperture.docx.Docx;

public class Module implements TraversalUtil.Callback {
	String id;

	Docx doc;

	Map<Object, Object> theirParent = new HashMap<Object, Object>();

	boolean initialized = false;

	public void init(Object head) throws InvalidFormatException {
		doc = new Docx();

		if (head != null) {
			doc.getBody().getContent().add(head);
			new TraversalUtil(head, this);

			theirParent.put(head, doc.getBody());
		}

		initialized = true;
	}

	// looks cool
	public void tail(Object o, Class<?> containerType) {
		if (containerType == null) {
			return;
		}

		List<Object> siblings = Docx.getPartsByClass(doc.getBody(),
				containerType);
		if (siblings.size() > 0) {
			ContentAccessor ca = (ContentAccessor) siblings
					.get(siblings.size() - 1);
			ca.getContent().add(o);

			theirParent.put(o, ca);
		} else {
			doc.getBody().getContent().add(o);
			theirParent.put(o, doc.getBody());

			//System.out.println(o.getClass().getName());
		}
	}

	@Override
	public void walkJAXBElements(Object parent) {

		List<Object> children = getChildren(parent);
		if (children != null) {
			for (Object o : children) {
				// if its wrapped in javax.xml.bind.JAXBElement, get its
				// value; this is ok, provided the results of the Callback
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
		return null;
	}

}
