package models.template;

/**
 * Created by mangix on 13-12-29.
 */


import java.util.*;

public class Module {
    public String name;
    public String text;

    public List<Object> list;



    public Module(String name, String text, List lists){
        this.name = name;
        this.text = text;
        this.list = lists;
    }

    public List<Question> getQuestionList(){
        List<Question> list= new ArrayList<Question>();

        if(this.list!=null){
            for(Object o:this.list){
                if(o instanceof Question){
                    list.add((models.template.Question)o);
                }
            }
        }

        return list;
    }

    public List<Module> getModuleList(){
        List<Module> list= new ArrayList<Module>();

        if(this.list!=null){
            for(Object o:this.list){
                if(o instanceof Module){
                    list.add((Module)o);
                }
            }
        }

        return list;
    }
}
