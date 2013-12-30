package models.mock;

/**
 * Created by mangix on 13-12-29.
 */


import java.util.*;

public class Module {
    public String name;
    public String text;

    public List<Object> lists;



    public Module(String name, String text, List lists){
        this.name = name;
        this.text = text;
        this.lists = lists;
    }

    public List<Question> getQuestionList(){
        List<Question> list= new ArrayList<Question>();

        if(this.lists!=null){
            for(Object o:this.lists){
                if(o instanceof Question){
                    list.add((models.mock.Question)o);
                }
            }
        }

        return list;
    }

    public List<Module> getModuleList(){
        List<Module> list= new ArrayList<Module>();

        if(this.lists!=null){
            for(Object o:this.lists){
                if(o instanceof Module){
                    list.add((Module)o);
                }
            }
        }

        return list;
    }
}
