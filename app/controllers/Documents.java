package controllers;

import com.aperture.docx.service.DocxService;
import models.*;
import models.template.*;
import play.mvc.*;
import views.html.dashboard;
import views.html.documents.*;

@Security.Authenticated(Secured.class)
public class Documents extends Controller {

    public static Result index() {
        return ok(index.render(models.Module.getDocuments()));

    }

    public static Result detail(Long id) {

        String name = models.Module.getById(id).name;

        models.template.Module document = DocxService.analyzeModule(name);

//        models.template.Module mockDocument = Data.generate();

        return ok(detail.render(document));

    }

    public static Result view() {
        return ok(dashboard.render(User.find.byId(session("email"))));
    }

}
