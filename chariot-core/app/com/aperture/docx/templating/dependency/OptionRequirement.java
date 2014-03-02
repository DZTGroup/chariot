package com.aperture.docx.templating.dependency;

import java.util.Map;

import models.Question;
import models.QuestionDescription;
import models.ModuleDependency;

public interface OptionRequirement implements Statement{
	String key;
	String required;
	
	public OptionRequirement( ModuleDependency dependency ){
		key = dependency.questionId;
		required = dependency.getRequiredOption();
	}
	
	boolean apply(Map<String, String> answers){
		// check
		if ( required == null ) return true;
		
		return answers.containsKey(key) && answers.get(key).equals(required);
	}
}