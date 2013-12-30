package com.aperture.docx.template.model;

import java.util.ArrayList;
import java.util.List;

public class Context {
	public static class Node {
		Node parent;
		List<Node> children = new ArrayList<Node>();
		
		protected void addChild(Node n){
			children.add(n);
			n.parent = this;
		}
		
		// copy and return
		public List<Node> getChildList(){
			return new ArrayList<Node>(children);
		}
	}
	
	Node ref;
}
