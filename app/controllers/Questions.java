package controllers;

import ajax.Ajax;
import com.google.gson.Gson;

import models.Question;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import java.util.HashMap;
import java.util.Map;


@Security.Authenticated(Secured.class)
public class Questions extends Controller {

    public static Result question(String id){

        Question question = Question.getById(id);

        Ajax ajax = new Ajax();

        if(question==null){
            ajax.setCode(404);
            ajax.setData(new String("no such question"));
        }else{
            ajax.setCode(200);

            Map<Object,Object> map = new HashMap<Object,Object>();
            map.put("id",question.id);
            map.put("description",question.description);
            map.put("type",question.type);

            ajax.setData(map);
        }

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
