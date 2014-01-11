package models;

//import java.util.*;
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
    
    @Lob
    public byte[] content;

    public static Model.Finder<Long, Module> find = new Model.Finder<Long, Module>(Long.class, Module.class);
}

