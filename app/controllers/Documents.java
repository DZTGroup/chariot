package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import models.*;
import views.html.*;
import views.html.documents.index;

@Security.Authenticated(Secured.class)
public class Documents extends Controller {
  
	 /**
     * Remove a project member.
     */
    public static Result index() {
        return ok(index.render(Document.getAllDocuments(), User.find.byId(request().username())));
    }
}
