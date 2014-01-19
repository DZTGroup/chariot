package models;

import com.aperture.docx.templating.api.DocxTemplatingService;
import com.google.gson.Gson;
import com.sun.org.apache.xalan.internal.xsltc.dom.ArrayNodeListIterator;
import models.template.Module;
import models.template.Question;

import java.util.ArrayList;
import play.Logger;
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

    public static PageContent parseFromDocumentId(Long id){
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
            Logger.info(e.getMessage());

        }

        return content;
    }

    public static void updatePaging(ArrayList<Long> documentList){
        //更新分页信息，如果模块有改动
        for(Long id: documentList){
            try{
                DocumentPaging p  = DocumentPaging.getByDocumentId(id);

                if(p!=null){
                    PageContent newContent = parseFromDocumentId(id);
                    PageContent oldContent = p.convertContent();


                    //删除没有的模块或者问题，更新新增的模块到默认分页
                    for(PageItem pageItem:newContent.pageList){
                        for(LittleModule module: pageItem.moduleList){
                            if(!oldContent.hasModule(module)){
                                oldContent.getDefaultPage().addModule(module);
                            }
                        }
                    }
                    //删除老的里面已经没有了的模块
                    for(PageItem pageItem:oldContent.pageList){
                        for(LittleModule module: pageItem.moduleList){
                            if(!newContent.hasModule(module)){
                                pageItem.moduleList.remove(module);
                            }
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



