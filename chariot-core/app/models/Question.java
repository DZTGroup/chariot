package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Project entity managed by Ebean
 */
@Entity 
public class Question extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public String id;

    public String description;

    public String type;



    public Question(String id, String description, String type) {
        //description is json
        //description.content is question content
        this.description = description;
        this.type = type;
        this.id = id;
    }

    // -- Queries

    public static Finder<String, Question> find = new Finder<String, Question>(String.class, Question.class);
    
    public static List<Question> getAllDocuments(){
    	return find.findList();
    }

    public static Question getById(String id){
        return find.byId(id);
    }

    public static Question create(String id, String description,String type){
        Question q = new Question(id,description,type);
        q.save();
        return q;
    }
}

