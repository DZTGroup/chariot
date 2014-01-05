package controllers;

import ajax.Ajax;
import com.aperture.docx.service.DocxService;
import models.*;
import play.mvc.*;
import views.html.dashboard;
import views.html.documents.*;

import java.util.List;
import java.util.Map;

@Security.Authenticated(Secured.class)
public class Documents extends Controller {
    final static private String DIR = "dir";
    final static private String DOC = "doc";

    public static Result index() {
        File root = new File("Root","dir");
        Long rootId = new Long("0");
        return ok(index.render(File.getFilesByParentId(rootId), root));
    } 
    public static Result folder(Long id){
        File file = File.getById(id);
        if(file!=null){
            if(file.type.equals(DIR)){
                List<File> files = File.getFilesByParentId(id);
                return ok(index.render(files,file));
            }else{
                return detail(file.name);
            }
        }else{
            return badRequest();
        }
    }

    public static Result detail(String name) {
        try{
            models.template.Module document = DocxService.analyzeModule(name);
            return ok(detail.render(document));
        }catch(Exception e){
            return badRequest();
        }



    }

    public static Result view() {
        return ok(dashboard.render(User.find.byId(session("email"))));
    }

    public static Result changeDir(){

        Http.RequestBody body = request().body();
        Map<String ,String[]> map = body.asFormUrlEncoded();

        String id = map.get("id")[0];
        String parentId = map.get("parentId")[0];

        File file = File.getById(Long.parseLong(id));

        Ajax ajax = new Ajax();

        file.parentId = Long.parseLong(parentId);
        file.save();

        ajax.setCode(200);
        ajax.setData(new String("success"));

        return ok(ajax.toJson());
    }

}
