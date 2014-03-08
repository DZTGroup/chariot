package models;

//import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;

import java.util.List;

import com.avaje.ebean.annotation.CacheStrategy;

/**
 * Project entity managed by Ebean
 */
@CacheStrategy (readOnly = false)
@Entity 
public class Module extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;
    
	@Constraints.Required
    public String name;
    
    @Constraints.Required
    public String gfsId;



    public static Model.Finder<Long, Module> find = new Model.Finder<Long, Module>(Long.class, Module.class);
}

