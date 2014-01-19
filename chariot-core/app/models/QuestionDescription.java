package models;

import com.google.gson.Gson;

/**
 * Created by mangix on 13-12-29.
 */
public class QuestionDescription {
    public String content;
    public String[] options;
    public String restrict;

    public QuestionDescription(String content,String[] options,String restrict){
        this.content = content;
        this.options = options;
        this.restrict = restrict;

    }

    public static QuestionDescription parse(String str){
        Gson gson = new Gson();
        return gson.fromJson(str,QuestionDescription.class);
    }
}
