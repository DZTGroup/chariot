package controllers;

import com.aperture.docx.templating.api.DocxTemplatingService;
import models.File;
import models.*;
import models.PageContent;
import models.template.Module;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

/**
 * Created by maquanhua on 1/11/14.
 */
public class Page extends Controller {

    public static Result index(Long id) {

        try {
            Module document =  DocxTemplatingService.analyzeModule(id);

            PageContent content  = null;

            DocumentPaging p  = DocumentPaging.getByDocumentId(id);
            if(p!=null){
                content = p.convertContent();
            }else{
                content  = PageContent.parseDocumentToPageContent(document);
            }

            return ok(page.render(content,document));
        } catch (Exception e) {
            return badRequest();
        }
    }
}
