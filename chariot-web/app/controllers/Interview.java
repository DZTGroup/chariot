package controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import models.EndUser;
import models.PageContent;
import models.Question;
import models.template.Module;
import play.mvc.*;
import views.html.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;

import com.aperture.docx.api.DocxTemplatingService;
import org.docx4j.openpackaging.exceptions.Docx4JException;

/**
 * Created by maquanhua on 1/18/14.
 * Moded by laomao
 */
@Security.Authenticated(WebSecured.class)
public class Interview extends Controller {

    public static Result question(Long documentId, int pageId) {

        PageContent content = PageContent.parseFromDocumentId(documentId);

        // TODO: ugly
        models.Module doc = models.Module.find.select("id,name").where().idEq(documentId).findUnique();

        if (content != null) {
            if (pageId < content.pageList.size()) {
                //根据问题答案生成过滤后的问题列表
                final List<Question> questions = new ArrayList<Question>();
                PageContent.PageItem pageItem = content.pageList.get(pageId);
                for (PageContent.LittleModule lm : pageItem.moduleList) {
                    if (lm.type.equals(PageContent.LittleModule.QUESTION)) {
                        questions.add(Question.getById(lm.id));
                    } else if (lm.type.equals(PageContent.LittleModule.MODULE)) {
                        try {
                            Module module = DocxTemplatingService.analyzeModule(Long.parseLong(lm.id));
                            if (!module.statement.apply()) {
                                break;
                            }
                            Module.travers(module, new Module.TraversImpl() {
                                @Override
                                public void apply(models.template.Question question) {
                                    questions.add(Question.getById(question.questionId));
                                }

                                @Override
                                public void apply(Module module) {

                                }

                                @Override
                                public boolean shouldEnter(Module module) {
                                    return module.statement.apply();
                                }
                            });
                        } catch (Exception e) {
                        }

                    }

                }

                return ok(interview.render(pageId, documentId, doc.name, content.pageList.get(pageId), content.pageList.size(), EndUser.findByEmail(session("email"))));
            } else {
                return badRequest();
            }
        } else {
            return badRequest();
        }
    }

    public static Result submit(Long documentId) throws IOException, Docx4JException {
        Gson gson = new Gson();

        Http.RequestBody body = request().body();
        Map<String, String[]> post = body.asFormUrlEncoded();

        String answer = post.get("answer")[0];
        //Map map = gson.fromJson(answer, Map.class);

        String path = DocxTemplatingService.getFinalDoc(documentId,
                gson.<Map<String, String>>fromJson(answer, new TypeToken<Map<String, String>>() {
                }.getType()));
        if (path != null) {
            response().setHeader("Content-Disposition",
                    "attachment;filename=\"generated\"");

            return ok(new File(path));
        }

        return notFound();
    }

    private void filterPage(List<Question> questions, PageContent.PageItem pageItem) {


    }
}


