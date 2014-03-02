package models;

//import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.Logger;

import play.data.validation.*;
import scala.Int;
import sun.util.logging.resources.logging;

import java.util.List;

/**
 * Project entity managed by Ebean
 */
@Entity
public class ModuleDependency extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long moduleId;

    @Constraints.Required
    public long ruleId;

    @Constraints.Required
    public String questionId;

    @Constraints.Required
    public long optionId;

    @ManyToOne
    public Question question;

    public ModuleDependency(){
    }

    public static Model.Finder<Long, ModuleDependency> find = new Model.Finder<Long, ModuleDependency>(Long.class, ModuleDependency.class);


    public static List<ModuleDependency> findByModuleId(Long id){
        return find.fetch("question").where().eq("module_id",id).findList();
    }
	
	// resolve dependency
	public String getRequiredOption(){
		String[] options = this.question.getQuestionDescription().options;
		
		if ( options != null && this.optionId < options.length ){
			return options[this.optionId];
		}
		
		Logger.error("no valid module dependency, id:" + id);
		
		return null;
	}
}

