package com.aperture.docx.template.model;

import java.util.List;

public class Block extends Context.Node{
	List<Object> documentObjects;
	
	public Block(List<Object> l){
		documentObjects = l;
	}
	
	public void addQuestion(Question q){
		this.addChild(q);
	}
	
	public void addModule(Block m){
		this.addChild(m);
	}
}
