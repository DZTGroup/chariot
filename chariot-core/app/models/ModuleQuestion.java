package models;

//import java.util.*;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

import com.avaje.ebean.annotation.CacheStrategy;

/**
 * Project entity managed by Ebean
 */

@CacheStrategy (readOnly = false)
@Entity
public class ModuleQuestion extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long moduleId;


    @Constraints.Required
    public String questionId;


    @OneToOne
    @JoinColumn(name="question_id",insertable = false,updatable = false)
    public Question question;

    public ModuleQuestion(){

    }


    public static Finder<Long, ModuleQuestion> find = new Finder<Long, ModuleQuestion>(Long.class, ModuleQuestion.class);


    public static List<ModuleQuestion> findByModuleId(Long id){
        return find.setUseQueryCache(true).setReadOnly(false).fetch("question").where().eq("moduleId",id).findList();
    }


}

