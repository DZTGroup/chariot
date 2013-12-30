package controllers;

import ajax.Ajax;
import com.google.gson.Gson;
import models.Module;

import models.Question;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;


@Security.Authenticated(Secured.class)
public class Questions extends Controller {

    public static Result question(Long id){

        Question question = Question.getById(id);

        Ajax ajax = new Ajax();

        if(question==null){
            ajax.setCode(404);
            ajax.setData(new String("no such question"));
        }else{
            ajax.setCode(200);
            ajax.setData(question);
        }

        return ok(ajax.toJson());

    }
}
