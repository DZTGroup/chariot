package controllers;

import models.Question;
import models.template.*;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import sun.util.calendar.LocalGregorianCalendar;
import util.Ajax;
import models.*;
import play.Logger;
import play.mvc.*;
import util.DependencyAnalyzer;
import views.html.dashboard;
import views.html.documents.*;

import java.util.*;

import com.aperture.docx.api.DocxTemplatingService;

@Security.Authenticated(Secured.class)
public class Documents extends Controller {
    final static private String DIR = "dir";
    final static private String DOC = "doc";

    public static Result index() {
        File root = new File("所有文档", "dir", null);
        root.id = Long.parseLong("0");
        Long rootId = new Long("0");
        return ok(index.render(File.getFilesByParentId(rootId), root));
    }

    public static Result folder(Long id) {
        File file = File.getById(id);
        if (file != null) {
            if (file.type.equals(DIR)) {
                List<File> files = File.getFilesByParentId(id);
                return ok(index.render(files, file));
            } else if (file.type.equals(DOC)) {
                return detail(id, file.documentId);
            }
        }

        return badRequest();
    }

    public static Result detail(Long id, Long documentId) {
        try {
            models.template.Module document = DocxTemplatingService
                    .analyzeModule(documentId);

            return ok(detail.render(document));
        } catch (Exception e) {
            Logger.error("[error]", e);
            return internalServerError("oops");
        }
    }

    public static Result view() {
        return ok(dashboard.render(User.find.byId(session("email"))));
    }

    public static Result changeDir() {

        Http.RequestBody body = request().body();
        Map<String, String[]> map = body.asFormUrlEncoded();

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

    public static Result delete() {

        Http.RequestBody body = request().body();
        Map<String, String[]> map = body.asFormUrlEncoded();
        Ajax ajax = new Ajax();

        String id = map.get("id")[0];
        File file = File.getById(Long.parseLong(id));
        if (file != null && file.type.equals("dir")) {
            List<File> files = File.getFilesByParentId(file.id);
            if (files.isEmpty()) {
                file.delete();
                ajax.setCode(200);
                ajax.setData(new String("删除成功"));
            } else {
                ajax.setCode(500);
                ajax.setData(new String("该目录不为空,不能删除"));
            }
        } else {
            file.delete();
            ajax.setCode(200);
            ajax.setData(new String("删除成功"));
        }

        return ok(ajax.toJson());
    }

    public static Result createFolder() {

        Http.RequestBody body = request().body();
        Map<String, String[]> map = body.asFormUrlEncoded();
        Ajax ajax = new Ajax();

        String parentId = map.get("parentId")[0];
        String name = map.get("name")[0];

        File file = new File(name, "dir", null);
        file.parentId = Long.parseLong(parentId);

        file.save();
        ajax.setCode(200);
        ajax.setData(new String("创建成功"));
        return ok(ajax.toJson());
    }

    public static Result pdf(String name) {
        response().setHeader("Content-Type", "application/pdf");
        return ok(new java.io.File(settings.Constant.USER_DIR + "/" + name + ".pdf"));
    }

    public static Result getPreviewUrl() {

        Http.RequestBody body = request().body();
        Map<String, String[]> map = body.asFormUrlEncoded();
        Ajax ajax = new Ajax();

        String id = map.get("id")[0];

        //
        try {
            String url = DocxTemplatingService.getPdfPreview(Long.parseLong(id));
            ajax.setCode(200);
            ajax.setData(url);
            return ok(ajax.toJson());
        } catch (Docx4JException e) {

            Logger.error(e.getMessage());
            return internalServerError();
        }
    }

    public static Result getDependency() {

        Http.RequestBody body = request().body();
        Map<String, String[]> map = body.asFormUrlEncoded();
        Ajax ajax = new Ajax();

        String id = map.get("documentId")[0];
        String moduleId = map.get("moduleId")[0];

        DependencyAnalyzer da = new DependencyAnalyzer(Long.parseLong(moduleId));

        Map<String, Object> result = new HashMap<String, Object>();

        result.put("rules", da.findRules());
        result.put("questionList", availableQuestionList(Long.parseLong(id), Long.parseLong(moduleId)));

        ajax.setCode(200);
        ajax.setData(result);
        return ok(ajax.toJson());
    }

    public static Result addCondition() {
        //为依赖添加一条condition
        Http.RequestBody body = request().body();
        Map<String, String[]> map = body.asFormUrlEncoded();
        Ajax ajax = new Ajax();

        String moduleId = map.get("moduleId")[0];
        String ruleId = map.get("ruleId")[0];
        String questionId = map.get("questionId")[0];
        String optionId = map.get("optionId")[0];

        ModuleDependency md = new ModuleDependency();
        md.moduleId = Long.parseLong(moduleId);
        md.questionId = questionId;
        md.optionId = Long.parseLong(optionId);
        if (ruleId.isEmpty()) {
            //新增一个rule
            //获取当前最大的ruleid
            List<ModuleDependency> dependencies = ModuleDependency.findByModuleId(md.moduleId);
            Long maxRuleId = new Long(0);
            for (ModuleDependency dependency : dependencies) {
                if (dependency.ruleId > maxRuleId) {
                    maxRuleId = dependency.ruleId;
                }
            }
            md.ruleId = ++maxRuleId;
        } else {
            md.ruleId = Long.parseLong(ruleId);
        }
        md.save();

        ajax.setCode(200);
        return ok(ajax.toJson());
    }

    public static Result deleteCondition() {
        //为依赖添加一条condition
        Http.RequestBody body = request().body();
        Map<String, String[]> map = body.asFormUrlEncoded();
        Ajax ajax = new Ajax();

        String id = map.get("id")[0];

        ModuleDependency md = ModuleDependency.find.byId(Long.parseLong(id));
        if (md != null) {
            md.delete();
        }

        ajax.setCode(200);
        return ok(ajax.toJson());
    }

    private static List<Question> availableQuestionList(Long documentId, Long moduleId) {
        //获取一个模块可依赖的问题list
        List<Question> list = new ArrayList<Question>();

        try {
            //文档中的所有问题
            Date now = new Date();
            models.template.Module document = DocxTemplatingService.analyzeModule(documentId);
            list = document.getQuestionExcept(moduleId);

            //文档级别的问题
            for (ModuleQuestion mq : ModuleQuestion.findByModuleId(documentId)) {
                if (mq.question != null) {
                    list.add(mq.question);
                }

            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        return list;
    }
}
