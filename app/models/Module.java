package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;

/**
 * Project entity managed by Ebean
 */
@Entity 
public class Module extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;
    
    public String name;

    public String type;
    
    public String path;
    
    public Module(String name, String type, String path) {
        this.name = name;
        this.type = type;
        this.path = path;
    }
    
    // -- Queries
    
    public static Model.Finder<Long, Module> find = new Model.Finder<Long, Module>(Long.class, Module.class);
    
    public static List<Module> getDocuments(){
    	return find.where().eq("type","doc").findList();
    }

    public static Module getById(long id){
        return find.byId(id);
    }
}

