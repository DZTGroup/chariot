package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.annotation.CacheStrategy;

/**
* Project entity managed by Ebean
*/
@CacheStrategy (readOnly = false)
	@Entity 
public class Question extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public String id;

	public String description;

	public String type;


	public Question(String id){
		this.id = id;
		this.type = "empty";
		this.description = "{}";
	}
	
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
	
	public static void batchImportQuestions(List<String> idList){
		Ebean.beginTransaction();
		try { 
			List<Question> list = getAllDocuments();
			
			
			for (String id :idList){
				boolean found = false;
				for ( Question q:list ){
					if (q.id.equals(id)){
						found = true;
						break;
					}
				}
				if(!found)(new Question(id)).save();
			}
			
			Ebean.commitTransaction();
		} finally { 
			// rollback if we didn't commit 
			// i.e. an exception occurred before commitTransaction(). 
			Ebean.endTransaction(); 
		}
	}
	
	public boolean isEmpty() {
		return this.type.equals("empty");
	}
	
	// get Question Description
	public QuestionDescription getQuestionDescription(){
		return QuestionDescription.parse(this.description);
	}
}

