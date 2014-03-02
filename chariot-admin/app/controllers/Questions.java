package controllers;

import models.ModuleQuestion;
import util.Ajax;
import models.Question;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import util.QuestionRestrictHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Security.Authenticated(Secured.class)
public class Questions extends Controller {

    public static Result question(String id){

        Question question = Question.getById(id);

        Ajax ajax = new Ajax();
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("id",id);

        if(question==null){
            map.put("description","{}");
            map.put("type","");
        }else{
            map.put("description",question.description);
            map.put("type",question.type);
        }
        map.put("restricts",new QuestionRestrictHelper().getList());
        ajax.setCode(200);
        ajax.setData(map);

        return ok(ajax.toJson());
    }

    public static Result saveQuestion(){
        Http.RequestBody body = request().body();
        Map<String ,String[]> map = body.asFormUrlEncoded();

        String id = map.get("id")[0];
        String description = map.get("description")[0];
        String type = map.get("type")[0];
        if(id.equals("new")){
            //新增的问题
            id = UUID.randomUUID().toString();
        }

        Question q = Question.getById(id);
        if(q==null){
            q = new Question(id,description,type);
        }else{
            q.description = description ;
            q.type =type;
        }

        q.save();
        Ajax ajax = new Ajax();
        ajax.setCode(200);
        ajax.setData(q);
        return ok(ajax.toJson());
    }

    public static Result getDocumentQuestions(Long id){
        //获取文档级的问题
        List<ModuleQuestion> moduleQuestions = ModuleQuestion.findByModuleId(id);

        Ajax ajax = new Ajax();

        ajax.setCode(200);
        ajax.setData(moduleQuestions);

        return ok(ajax.toJson());
    }

    public static Result saveModuleQuestion(){
        Http.RequestBody body = request().body();
        Map<String ,String[]> map = body.asFormUrlEncoded();

        String moduleId = map.get("moduleId")[0];
        String questionId = map.get("questionId")[0];

        ModuleQuestion mq = new ModuleQuestion();
        mq.moduleId = Long.parseLong(moduleId) ;
        mq.questionId = questionId;
        mq.save();
        Ajax ajax = new Ajax();
        ajax.setCode(200);
        ajax.setData(mq);
        return ok(ajax.toJson());
    }


}
