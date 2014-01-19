package controllers;

import com.aperture.docx.templating.api.DocxTemplatingService;
import models.DocumentPaging;
import models.PageContent;
import models.template.Module;
import play.mvc.*;
import views.html.*;

/**
 * Created by maquanhua on 1/18/14.
 */

public class Interview extends Controller {

    public static Result question(Long documentId, int pageId) {

        PageContent content = PageContent.parseFromDocumentId(documentId);

        if (content != null) {
            if(pageId < content.pageList.size()){
                return ok(interview.render(content.pageList.get(pageId),content.pageList.size()));
            }else {
                return badRequest();
            }
        } else {
            return badRequest();
        }
    }
}
