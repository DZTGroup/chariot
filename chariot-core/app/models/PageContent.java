package models;

import models.template.Module;
import models.template.Question;

import java.util.ArrayList;

/**
 * Created by maquanhua on 1/11/14.
 */
public class PageContent {
    public ArrayList<PageItem> pageList;

    public PageContent(ArrayList<PageItem> pageList){
        this.pageList = pageList;
    }

    public static PageContent parseDocumentToPageContent(Module document){
        PageContent pageContent = new PageContent(new ArrayList<PageItem>());
        pageContent.parseNewModule(document);

        return pageContent;
    }


    public boolean hasModule(Object module){
        //是否已经有这个模块或者问题了
        for(int i=0;i<pageList.size();i++){
            PageItem item = pageList.get(i);
            if(item.hasModule(module)){
                return true;
            }
        }
        return false;
    }

    public void parseNewModule(Module document){
        //把不在PageContent里面的module默认放到默认分页里面
        PageItem defaultPage = getDefaultPage();
        for(int i=0;i<document.list.size();i++){
            if(!hasModule(document.list.get(i))){
                defaultPage.addModule(document.list.get(i));
            }
        }

    }

    public PageItem getDefaultPage(){
        for(int i=0;i<pageList.size();i++){
            if(pageList.get(i).name.equals("默认分页")){
                return pageList.get(i);
            }
        }
        PageItem defaultPage = new PageItem("默认分页","这是一个默认分页",new ArrayList<LittleModule>());
        pageList.add(defaultPage);
        return defaultPage;
    }
}



