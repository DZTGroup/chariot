package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;

import com.avaje.ebean.*;

/**
 * Project entity managed by Ebean
 */
@Entity 
public class Document extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;
    
    public String name;
    
    public String path;
    
    public Document(String name, String path) {
        this.name = name;
        this.path = path;
    }
    
    // -- Queries
    
    public static Model.Finder<Long,Document> find = new Model.Finder<Long,Document>(Long.class, Document.class);
    
    public static List<Document> getAllDocuments(){
    	return find.findList();
    }
    
}

