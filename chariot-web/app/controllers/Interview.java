package controllers;

import com.aperture.docx.templating.api.DocxTemplatingService;
import models.DocumentPaging;
import models.PageContent;
import models.template.Module;
import play.mvc.*;
import views.html.*;

import play.Logger;

/**
 * Created by maquanhua on 1/18/14.
 */

public class Interview extends Controller {

    public static Result question(Long documentId, int pageId) {

        PageContent content = PageContent.parseFromDocumentId(documentId);
		
		// TODO: ugly
		models.Module doc = models.Module.find.select("id,name").where().idEq(documentId).findUnique();

        if (content != null) {
            if(pageId < content.pageList.size()){
                return ok(interview.render(pageId,documentId, doc.name, content.pageList.get(pageId),content.pageList.size()));
            }else {
                return badRequest();
            }
        } else {
            return badRequest();
        }
    }
}
