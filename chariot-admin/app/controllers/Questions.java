package controllers;

import util.Ajax;
import models.Question;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import util.QuestionRestrictHelper;

import java.util.HashMap;
import java.util.Map;


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
        Question q = Question.getById(id);
        if(q==null){
            q = new Question(id,description,type);
        }else{
            q.description = description ;
            q.type =type;
        }

        q.save();
        return ok();
    }


}
