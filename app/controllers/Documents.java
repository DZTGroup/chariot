package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import models.*;
import views.html.*;
import views.html.documents.*;

import javax.print.Doc;

@Security.Authenticated(Secured.class)
public class Documents extends Controller {
  
    public static Result index() {
        return ok(index.render(Document.getAllDocuments()));

    }

    public static Result detail(Long id){
        return ok(detail.render(Document.getById(id)));

    }
}
