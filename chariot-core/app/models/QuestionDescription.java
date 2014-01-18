package models;

import com.google.gson.Gson;

/**
 * Created by mangix on 13-12-29.
 */
public class QuestionDescription {
    public String content;
    public String[] options;

    public QuestionDescription(String content,String[] options){
        this.content = content;
        this.options = options;

    }

    public static QuestionDescription parse(String str){
        Gson gson = new Gson();
        return gson.fromJson(str,QuestionDescription.class);
    }
}
