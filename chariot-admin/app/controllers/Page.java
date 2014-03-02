package controllers;

import com.aperture.docx.api.DocxTemplatingService;
import com.google.gson.Gson;
import models.File;
import models.*;
import models.PageContent;
import models.template.Module;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import util.Ajax;
import views.html.*;

import java.util.Map;

/**
 * Created by maquanhua on 1/11/14.
 */
public class Page extends Controller {

    public static Result index(Long id) {
        try {
            PageContent content = PageContent.parseFromDocumentId(id);
            Module document =  DocxTemplatingService.analyzeModule(id);
            return ok(page.render(content,document));
        }catch (Exception e){
            return badRequest();
        }
    }

    public static Result save() {

        Http.RequestBody body = request().body();
        Map<String, String[]> map = body.asFormUrlEncoded();

        String documentId = map.get("id")[0];
        String content = map.get("content")[0];

        DocumentPaging p = DocumentPaging.getByDocumentId(Long.parseLong(documentId));
        if(p!=null){
            p.content = content;
        }else{
            p = new DocumentPaging(Long.parseLong(documentId),content);
        }
        p.save();

        Ajax ajax = new Ajax();

        ajax.setCode(200);
        ajax.setData(new String("success"));

        return ok(ajax.toJson());
    }
}
