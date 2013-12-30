package controllers;

import models.template.*;
import play.mvc.*;
import views.html.dashboard;
import views.html.documents.*;
import models.User;

@Security.Authenticated(Secured.class)
public class Documents extends Controller {
  
    public static Result index() {
        return ok(index.render(models.Module.getDocuments()));

    }

    public static Result detail(Long id){
        models.template.Module mockDocument = Data.generate();


        return ok(detail.render(mockDocument));

    }
    
    public static Result view(){
        return ok(dashboard.render(User.find.byId(session("email"))));
    }

}
