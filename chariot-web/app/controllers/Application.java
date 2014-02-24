package controllers;

import static play.data.Form.form;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import configs.Constant;

import models.EndUser;
import play.cache.Cache;
import play.data.Form;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {
    
 // -- Authentication

 	public static class Login {

 		public String email;
 		public String password;

 		public String validate() {
 			EndUser user=EndUser.authenticate(email, password);
 			if ( user== null) {
 				return "Invalid user or password";
 			}
 			else if(!user.status.equals("1")){
 				return "inactive user account, please active the account first!";
 			}
 			return null;
 		}

 	}
 	
 	public static class Register {
 		
 		public String name;
 		public String email;
 		public String password;
 		
 		public String validate() {
 			if (EndUser.findByEmail(email) != null) {
 				return "email is already used for another account";
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
 			return redirect(routes.MainApp.index());
 		}
 	}
 	
 	public static Result registerAccount() throws java.lang.Exception{
 		Form<Register> registerForm = form(Register.class).bindFromRequest();
 		if (registerForm.hasErrors()) {
 			return badRequest(register.render(registerForm));
 		} else {
 			//UUID uuid = UUID.randomUUID();
 			EndUser.create(registerForm.get().email,registerForm.get().name,registerForm.get().password);
 			String url=null; 			
 			UUID uuid=UUID.randomUUID();
 			Cache.set(uuid.toString(),registerForm.get().email, 15 * 60);
 			url=Constant.WEBSITE_ADDRESS+"/verify/"+uuid;
 			//generate a register verify url
 			return ok(registerSuccess.render(url));
 			
 			//todo:call the java mail interface to send a email that contains this url to the user's register email address
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
		
 		return ok(register.render(form(Register.class)));
 	}
	
	public static Result verify(String uuid){
		//hanlde the  status update and redirect the user to the main page with the login state
		Object o=Cache.get(uuid);
		if(o!=null){
			EndUser user=EndUser.updateStatus(o.toString(), "1");
			session("email", user.email);
 			return redirect(routes.MainApp.index());
		}
		else{
			return badRequest("该链接已经失效！");
		}		
	}

}
