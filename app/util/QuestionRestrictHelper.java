package util;


import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by maquanhua on 1/7/14.
 */
public class QuestionRestrictHelper {
    //all the restricts

    private static final String NUMBER = "number";
    private static final String NUMBER_NAME = "数字";
    private static final String NUMBER_PATTERN = "\\d+";
    private static final String DATE = "date";
    private static final String DATE_NAME = "日期";
    private static final String DATE_PATTERN = "(\\d{4}-\\d{2}-\\d{2})|(\\d{4}/\\d{2}/\\d{2})";

    private ArrayList<QuestionRestrict> list;

    public QuestionRestrictHelper(){
        list = new ArrayList<>();
        list.add(new QuestionRestrict( NUMBER,NUMBER_NAME,Pattern.compile(NUMBER_PATTERN)));
        list.add(new QuestionRestrict(DATE,DATE_NAME,Pattern.compile(DATE_PATTERN)));
    }

    public ArrayList<QuestionRestrict> getList(){
        return list;
    }

    public String getPatternStringByType(String type){
        for(int i=0;i<list.size();i++){
            if(list.get(i).getType().equals(type)){
                return list.get(i).getPattern().toString();
            }
        }
        return null;
    }

}
