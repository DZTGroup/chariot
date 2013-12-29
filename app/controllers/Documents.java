package controllers;

import play.mvc.*;
import models.*;
import views.html.documents.*;

@Security.Authenticated(Secured.class)
public class Documents extends Controller {
  
    public static Result index() {
        return ok(index.render(Module.getDocuments()));

    }

    public static Result detail(Long id){
        return ok(detail.render(Module.getById(id)));

    }
}
