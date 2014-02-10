package controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import models.PageContent;
import play.mvc.*;
import views.html.*;

import java.util.Map;
import java.io.File;
import java.io.IOException;

import com.aperture.docx.templating.api.DocxTemplatingService;
import org.docx4j.openpackaging.exceptions.Docx4JException;

/**
 * Created by maquanhua on 1/18/14.
 */

public class Interview extends Controller {

    public static Result question(Long documentId, int pageId) {

        PageContent content = PageContent.parseFromDocumentId(documentId);

        // TODO: ugly
        models.Module doc = models.Module.find.select("id,name").where().idEq(documentId).findUnique();

        if (content != null) {
            if (pageId < content.pageList.size()) {
                return ok(interview.render(pageId, documentId, doc.name, content.pageList.get(pageId), content.pageList.size()));
            } else {
                return badRequest();
            }
        } else {
            return badRequest();
        }
    }

    public static Result submit(Long documentId) throws IOException, Docx4JException{
        Gson gson = new Gson();

        Http.RequestBody body = request().body();
        Map<String, String[]> post = body.asFormUrlEncoded();

        String answer = post.get("answer")[0];
        //Map map = gson.fromJson(answer, Map.class);

		String name = DocxTemplatingService.getFinalDoc(documentId, 
			gson.<Map<String, String>>fromJson(answer, new TypeToken<Map<String, String>>(){}.getType()));
		if ( name != null ){
			response().setHeader("Content-Disposition",
					"attachment;filename=\"" + name + "\"");
			
			return ok(new File(settings.Constant.USER_DIR + "/" + name + ".docx"));
		}

        return badRequest();
    }
}


