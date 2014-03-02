package com.aperture.docx.templating.dependency;

import java.util.List;
import java.util.Map;

public class Or implements Statement {
	
	List<Statement> slist;
	
	public Or(List<Statement> l){
		this.slist = l;
	}
	
	boolean apply(Map<String, String> answers){
		// true for empty list
		if( slist.size() == 0 ) return true;
		
		for ( Statement s: slist ){
			if ( s.apply(answers) ){
				return true;
			}
		}
		
		return false;
	}
}