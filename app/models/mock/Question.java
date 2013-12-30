package models.mock;

/**
 * Created by mangix on 13-12-29.
 */

public class Question {
    public Long questionId;
    public String context;

    public Question(Long questionId, String context){
        this.questionId = questionId;
        this.context = context;
    }
}
