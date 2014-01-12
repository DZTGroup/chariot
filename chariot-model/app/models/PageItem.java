package models;

import java.util.ArrayList;

/**
 * Created by maquanhua on 1/12/14.
 */
public class PageItem{
    public String name;
    public ArrayList<LittleModule> moduleList;

    PageItem(String name,ArrayList<LittleModule> moduleList){
        this.name = name;
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
