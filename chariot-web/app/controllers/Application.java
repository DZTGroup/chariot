package controllers;

import static play.data.Form.form;
import models.EndUser;
import models.File;
import play.*;
import play.data.Form;
import play.mvc.*;

import java.util.List;

import views.html.*;

// scala test
import com.aperture.docx.scala.Gfs;

public class Application extends Controller {

    public static Result index() {
        List<File> docs = File.find.where().isNotNull("document_id").findList();

        return ok(index.render(docs));
    }
    
 // -- Authentication

 	public static class Login {

 		public String email;
 		public String password;

 		public String validate() {
 			if (EndUser.authenticate(email, password) == null) {
 				return "Invalid user or password";
 			}
 			return null;
 		}

 	}

 	/**
 	 * Login page.
 	 */
 	public static Result login() {
 		return ok(login.render(form(Login.class)));
 	}

 	/**
 	 * Handle login form submission.
 	 */
 	public static Result authenticate() {
 		Form<Login> loginForm = form(Login.class).bindFromRequest();
 		if (loginForm.hasErrors()) {
 			return badRequest(login.render(loginForm));
 		} else {
 			session("email", loginForm.get().email);
 			return redirect(routes.Application.index());
 		}
 	}

 	/**
 	 * Logout and clean the session.
 	 */
 	public static Result logout() {
 		session().clear();
 		flash("success", "You've been logged out");
 		return redirect(routes.Application.login());
 	}
 	
	public static Result register() {
 		return ok(register.render());
 	}

}
