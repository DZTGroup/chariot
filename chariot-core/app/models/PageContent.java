package models;

import com.aperture.docx.api.DocxTemplatingService;
import com.google.gson.Gson;
import models.template.Module;
import models.template.Question;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import play.Logger;
/**
 * Created by maquanhua on 1/11/14.
 */
public class PageContent {
    public List<PageItem> pageList;

    public PageContent(List<PageItem> pageList){
        this.pageList = pageList;
    }

    public static PageContent parseDocumentToPageContent(Module document){
        PageContent pageContent = new PageContent(new ArrayList<PageItem>());
        pageContent.parseNewModule(document);

        return pageContent;
    }

    public static PageContent parseFromDocumentId(Long id){
        //如果这个稳定被Parse过就返回Document_Paging的结果，
        //不然就生成一个新的PageContent对象
        PageContent content  = null;

        try {
            Module document =  DocxTemplatingService.analyzeModule(id);

            DocumentPaging p  = DocumentPaging.getByDocumentId(id);

            if(p!=null){
                content = p.convertContent();
            }else{
                content  = PageContent.parseDocumentToPageContent(document);
            }

        } catch (Exception e) {
            Logger.error(e.getMessage());

        }

        return content;
    }

    public static void updatePaging(List<Long> documentList){
        //更新分页信息，如果模块有改动
        for(Long id: documentList){
            try{
                DocumentPaging p  = DocumentPaging.getByDocumentId(id);

                if(p!=null){
                    PageContent newContent = parseDocumentToPageContent(DocxTemplatingService.analyzeModule(id));
                    PageContent oldContent = p.convertContent();

                    ArrayList<LittleModule> tempAddList = new ArrayList<LittleModule>();
                    ArrayList<LittleModule> tempRemoveList = new ArrayList<LittleModule>();


                    //删除没有的模块或者问题，更新新增的模块到默认分页
                    for(PageItem pageItem:newContent.pageList){
                        for(LittleModule module: pageItem.moduleList){
                            if(!oldContent.hasModule(module)){
                                //oldContent.getDefaultPage().moduleList.add(module);
                                tempAddList.add(module);
                            }
                        }
                    }
                    for(LittleModule module:tempAddList){
                        oldContent.getDefaultPage().moduleList.add(module);
                    }

                    //删除老的里面已经没有了的模块
                    for(PageItem pageItem:oldContent.pageList){
                        for(LittleModule module: pageItem.moduleList){
                            if(!newContent.hasModule(module)){
                                //pageItem.moduleList.remove(module);
                                tempRemoveList.add(module);
                            }
                        }
                    }
                    for(LittleModule module:tempRemoveList){
                        for(PageItem pageItem:oldContent.pageList){
                            pageItem.moduleList.remove(module);
                        }
                    }

                    Gson gson = new Gson();
                    p.content = gson.toJson(oldContent);
                    p.save();
                }

            }catch (Exception e){
                Logger.info(e.getMessage());
            }

        }

    }

    public boolean hasModule(Object module){
        //是否已经有这个模块或者问题了
        for(PageItem item:pageList){
            if(item.hasModule(module)){
                return true;
            }
        }
        return false;
    }

    public void parseNewModule(Module document){
        //把不在PageContent里面的module默认放到默认分页里面
        PageItem defaultPage = getDefaultPage();
        for(Object module : document.list){
            if(!hasModule(module)){
                defaultPage.addModule(module);
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

    /**
     * Created by maquanhua on 1/12/14.
     */

    /*
    * 分页用的
    * json 转 Object
    *
    * */
    public static class LittleModule{
        public String id;
        public String type;

        LittleModule(String id,String type){
            this.id = id;
            this.type = type;
        }

        public static LittleModule parseModule(Object module){
            //把Module或者Question 转成LittleModule
            if(module instanceof Module){
                return new LittleModule(((Module)(module)).id.toString(),"module");
            }else if(module instanceof Question) {
                return new LittleModule(((Question)(module)).questionId,"question");
            }else{
                return (LittleModule)(module);
            }
        }
    }

    /**
     * Created by maquanhua on 1/12/14.
     */
    public static class PageItem{
        public String name;
        public String desc;
        public List<LittleModule> moduleList;

        PageItem(String name,String desc,List<LittleModule> moduleList){
            this.name = name;
            this.desc = desc;
            this.moduleList = moduleList;
        }

        public boolean hasModule(Object module){
            LittleModule littleModule = LittleModule.parseModule(module);
            for(LittleModule lm: moduleList){
                if(littleModule.id.equals(lm.id) && lm.type.equals(littleModule.type)){
                    return true;
                }
            }
            return false;
        }

        public void addModule(Object module){
            moduleList.add(LittleModule.parseModule(module));
        }
    }
}



