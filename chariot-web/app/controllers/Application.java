package controllers;

import static play.data.Form.form;
import models.EndUser;
import play.data.Form;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {
    
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
 	
 	public static Result registerAccount() {
 		Form<Register> registerForm = form(Register.class).bindFromRequest();
 		if (registerForm.hasErrors()) {
 			return badRequest(register.render(registerForm));
 		} else {
 			EndUser.create(registerForm.get().email,registerForm.get().name,registerForm.get().password);
 			return redirect(routes.Application.login());
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

}
