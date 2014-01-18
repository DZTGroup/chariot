package models;

import java.util.ArrayList;

/**
 * Created by maquanhua on 1/12/14.
 */
public class PageItem{
    public String name;
    public String desc;
    public ArrayList<LittleModule> moduleList;

    PageItem(String name,String desc,ArrayList<LittleModule> moduleList){
        this.name = name;
        this.desc = desc;
        this.moduleList = moduleList;
    }

    public boolean hasModule(Object module){
        for(int i=0;i<moduleList.size();i++){
            LittleModule lm = LittleModule.parseModule(module);
            if(moduleList.get(i).id == lm.id){
                return true;
            }
        }
        return false;
    }

    public void addModule(Object module){
        moduleList.add(LittleModule.parseModule(module));
    }
}
