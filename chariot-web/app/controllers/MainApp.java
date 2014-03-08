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

        return ok(index.render(docs,EndUser.findByEmail(session("email"))));
    }
}
