package models;

import models.template.*;
import models.template.Question;

/**
 * Created by maquanhua on 1/12/14.
 */

/*
* 分页用的
* json 转 Object
*
* */
public class LittleModule{
    public String id;
    public String type;

    LittleModule(String id,String type){
        this.id = id;
        this.type = type;
    }

    public static LittleModule parseModule(Object module){
        //把Module或者Question 转成LittleModule
        if(module instanceof models.template.Module){
            return new LittleModule(((models.template.Module)(module)).id.toString(),"module");
        }else {
            return new LittleModule(((Question)(module)).questionId,"question");
        }

    }
}
