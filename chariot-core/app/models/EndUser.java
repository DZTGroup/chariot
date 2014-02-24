package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

/**
 * User entity managed by Ebean
 */
@Entity 
@Table(name="end_user")
public class EndUser extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    @Constraints.Required
    @Formats.NonEmpty
    public String email;
    
    @Constraints.Required
    public String name;
    
    @Constraints.Required
    public String password;
    
    public String status;
    
    public EndUser(String email, String name, String password) {
        //description is json
        //description.content is question content
        this.email = email;
        this.name = name;
        this.password = password;
        this.status="0";
    }
    
    // -- Queries
    
    public static Model.Finder<String,EndUser> find = new Model.Finder<String,EndUser>(String.class, EndUser.class);
    
    /**
     * Retrieve all users.
     */
    public static List<EndUser> findAll() {
        return find.all();
    }

    /**
     * Retrieve a User from email.
     */
    public static EndUser findByEmail(String email) {
        return find.where().eq("email", email).findUnique();
    }
    
    /**
     * Authenticate a User.
     */
    public static EndUser authenticate(String email, String password) {
    	return find.where()
            .eq("email", email)
            .eq("password", password)
            .findUnique();
    }
    
    public static EndUser create(String email, String name, String password){
    	EndUser u = new EndUser(email,name,password);
        u.save();
        return u;
    }
    
    public static EndUser updateStatus(String email,String status){
    	EndUser u=findByEmail(email);
    	u.status=status;
    	u.save();
    	
    	return u;
    }
    
    // --
    
    public String toString() {
        return "User(" + email + ")";
    }

}

