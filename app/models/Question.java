package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

/**
 * Project entity managed by Ebean
 */
@Entity 
public class Question extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;

    public String description;

    public String type;

    public Question(String description, String type) {
        //description is json
        //description.content is question content
        this.description = description;
        this.type = type;
    }

    // -- Queries

    public static Finder<Long, Question> find = new Finder<Long, Question>(Long.class, Question.class);
    
    public static List<Question> getAllDocuments(){
    	return find.findList();
    }

    public static Question getById(long id){
        return find.byId(id);
    }

    public static Question create(String description,String type){
        Question q = new Question(description,type);
        q.save();
        return q;
    }
}

