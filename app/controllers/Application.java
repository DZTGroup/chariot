package controllers;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.aperture.docx.Docx;
import com.aperture.docx.dom.DocxTreeStructure;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import models.*;
import views.html.*;

public class Application extends Controller {
  
    // -- Authentication
    
    public static class Login {
        
        public String email;
        public String password;
        
        public String validate() {
            if(User.authenticate(email, password) == null) {
                return "Invalid user or password";
            }
            return null;
        }
        
    }

    /**
     * Login page.
     */
    public static Result login() {
        return ok(
            login.render(form(Login.class))
        );
    }
    
    /**
     * Handle login form submission.
     */
    public static Result authenticate() {
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        if(loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {
            session("email", loginForm.get().email);
            return redirect(
                routes.Application.index()
            );
        }
    }

    /**
     * Logout and clean the session.
     */
    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(
            routes.Application.login()
        );
    }
  
    // -- Javascript routing
    
    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
            Routes.javascriptRouter("jsRoutes",
                //Routes for Documents
                controllers.routes.javascript.Documents.index()                
            )
        );
    }

    public static Result index(){
        return ok(dashboard.render(User.find.byId(session("email"))));
    }
    
    // test stub by mao
    public static Result parse() throws Docx4JException{
    	String path = System.getProperty("user.dir") + "/template/upload/sample.docx";
		String outputPath = System.getProperty("user.dir") + "/template/upload/mod.docx";

		Docx doc = new Docx(path);
		new DocxTreeStructure(doc).parse();

		doc.save(outputPath);
		return ok("done!");
    }
}
