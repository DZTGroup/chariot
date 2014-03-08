package controllers;

import models.EndUser;
import models.File;
import models.User;
import play.mvc.*;

import java.util.List;

import views.html.*;
@Security.Authenticated(Secured.class)
public class MainApp extends Controller {

    public static Result index() {
        List<File> docs = File.find.where().isNotNull("document_id").findList();
        if(EndUser.findByEmail(session("email"))!=null){
        	return ok(index.render(docs,EndUser.findByEmail(session("email"))));
        }
        else{
        	return redirect(routes.Application.login());
        }
        
    }
}
