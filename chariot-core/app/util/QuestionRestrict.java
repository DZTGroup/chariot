package util;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by maquanhua on 1/7/14.
 */


public class QuestionRestrict {

    private String type;
    private String name;
    private Pattern pattern;

    public QuestionRestrict(String type,String name,Pattern pattern){
        this.type = type;
        this.name = name;
        this.pattern = pattern;
    }

    public String getType(){
        return type;
    }
    public String getName(){
        return name;
    }
    public Pattern getPattern(){
        return pattern;
    }

}

