package com.aperture.docx.templating.dependency;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import play.Logger;

import models.ModuleDependency;

public class DependencyFactory {
	// get all dependency for module id
	public static Statement createDependencyStatementFor(long id){
		
		Map< Long, List<Statement> > rulesMapping = new HashMap<Long, List<Statement>>();
		
		// parse db data
		for( ModuleDependency md: ModuleDependency.findByModuleId_c(id)){
			Statement statement = new OptionRequirement(md);
			
			if ( rulesMapping.containsKey(md.ruleId) ){
				rulesMapping.get(md.ruleId).add(statement);
			} else {
				List<Statement> rule = new ArrayList<Statement>();
				rule.add(statement);
				
				rulesMapping.put(md.ruleId, rule);
			}
		}
		
		// convert to statement
		List<Statement> ruleSet = new ArrayList<Statement>();
		for ( Map.Entry< Long, List<Statement> > rules : rulesMapping.entrySet() ){
			ruleSet.add( new And(rules.getValue()) );
		}
		
		return new Or(ruleSet);
	}
}