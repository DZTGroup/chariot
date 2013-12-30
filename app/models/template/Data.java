package models.template;

import java.util.*;

import models.template.Module;


/**
 * Created by mangix on 13-12-29.
 */
public class Data {
    public Data(){

    }

    public static Module generate(){
        String q1Context = "甲方___________";
        Question q1 = new Question(new Long(1),q1Context);

        List<Object> list1 = new ArrayList<Object>();
        list1.add(q1);


        Module module1 = new Module("module1","text",list1);
        Module module2 = new Module("module2","text",null);

        List<Object> lists = new ArrayList<Object>();
        lists.add(module1);
        lists.add(module2);

        Module document = new Module("老黑的卖身契","",lists);
        return document;
    }
}
