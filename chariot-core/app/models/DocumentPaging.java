package models;

import com.google.gson.Gson;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by maquanhua on 1/11/14.
 */
@Entity
public class DocumentPaging extends Model{
    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    public Long documentId;

    public String content;


    public DocumentPaging(Long documentId, String content) {
        this.documentId = documentId;
        this.content = content;
    }

    // -- Queries

    public static Model.Finder<Long, DocumentPaging> find = new Model.Finder<Long, DocumentPaging>(Long.class, DocumentPaging.class);

    public static DocumentPaging getByDocumentId(Long documentId){
        return find.where().eq("document_id",documentId).findUnique();
    }

    public PageContent convertContent(){
        Gson gson = new Gson();
        return gson.fromJson(this.content,PageContent.class);
    }


}
