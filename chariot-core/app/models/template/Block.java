package models.template;

/**
 * Created by mangix on 13-12-29.
 */


import java.util.*;

public class Block {
    public String name;
    public String text;

    public List<Object> list;



    public Block(String name, String text, List<Object> list){
        this.name = name;
        this.text = text;
        this.list = list;
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

    public List<Block> getModuleList(){
        List<Block> list= new ArrayList<Block>();

        if(this.list!=null){
            for(Object o:this.list){
                if(o instanceof Block){
                    list.add((Block)o);
                }
            }
        }

        return list;
    }
}
