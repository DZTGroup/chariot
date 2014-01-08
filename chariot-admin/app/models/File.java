package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

/**
 * Project entity managed by Ebean
 */
@Entity 
public class File extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;

    public String name;

    public String type;

    public Long parentId;

    public File(String name, String type) {
        this.name = name;
        this.type = type;
        this.parentId = Long.valueOf(0);
    }

    // -- Queries

    public static Finder<Long, File> find = new Finder<Long, File>(Long.class, File.class);
    
    public static List<File> getFilesByParentId(Long parentId){
    	return find.where().eq("parentId", parentId).findList();
    } 

    public static File getById(long id){
        return find.byId(id);
    }

}

