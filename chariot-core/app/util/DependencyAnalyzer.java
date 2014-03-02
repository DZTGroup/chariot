package util;

import models.Module;
import models.ModuleDependency;
import models.Question;
import models.QuestionDescription;
import play.Logger;

import java.util.*;

/**
 * Created by maquanhua on 2/23/14.
 */
public class DependencyAnalyzer {

    public Long moduleId;
    public List<ModuleDependency> dependencies;

    public DependencyAnalyzer(Long moduleId){
        this.moduleId = moduleId;
        this.dependencies = ModuleDependency.findByModuleId(moduleId);
    }

    public List<Rule> findRules(){
        List<Rule> rules = new ArrayList<Rule>();
        Map<Long,Rule> temp = new HashMap<Long,Rule>();
        for(ModuleDependency md: this.dependencies){
            Rule rule;
            if(!temp.containsKey(md.ruleId)){
                rule = new Rule(md.ruleId);
                temp.put(md.ruleId,rule);
                rules.add(rule);
            }else{
                rule = temp.get(md.ruleId);
            }
            rule.addCondition(md.question,md.optionId);
        }
        return rules;
    }


    class Rule{
        public Long id;
        public List<Map<String,Object>> conditions;

        public Rule(Long id){
            this.id = id;
            conditions = new ArrayList<Map<String,Object>>();
        }

        public void addCondition(Question question,Long optionIndex){
            QuestionDescription qd = QuestionDescription.parse(question.description);
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("questionId",question.id);
            map.put("questionContent",qd.content);
            map.put("questionOptions",qd.options);
            map.put("questionSelect",optionIndex);

            conditions.add(map);
        }
    }
}
