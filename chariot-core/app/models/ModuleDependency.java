package models;

//import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
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
    @JoinColumn(name="question_id",insertable = false,updatable = false)
    public Question question;

    public ModuleDependency(){

    }

    public static Model.Finder<Long, ModuleDependency> find = new Model.Finder<Long, ModuleDependency>(Long.class, ModuleDependency.class);


    public static List<ModuleDependency> findByModuleId(Long id){
        return find.fetch("question").where().eq("module_id",id).findList();
    }


}

