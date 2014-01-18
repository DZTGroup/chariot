package controllers;
import models.DocumentPaging;
import models.PageContent;
import play.mvc.*;
import views.html.*;

/**
 * Created by maquanhua on 1/18/14.
 */

public class Interview extends Controller{

    public static Result question(Long documentId, Long pageId){

        
        return ok(interview.render());
    }
}
