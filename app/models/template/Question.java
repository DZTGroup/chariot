package models.template;

/**
 * Created by mangix on 13-12-29.
 */

public class Question {
    public String questionId;
    public String context;

    public Question(String questionId, String context){
        this.questionId = questionId;
        this.context = context;
    }
}
