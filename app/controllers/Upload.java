package controllers;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import views.html.*;
import views.html.upload.*;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class Upload extends Controller {
	
	public static Result index() {
        return ok(view.render("welcome"));
    }

    public static Result uploadDocument(String fileName) {
        File file = request().body().asRaw().asFile();
        String myUploadPath = Play.application().configuration().getString("uploadFilePath");
        File newFile = new File(myUploadPath,fileName);
        Logger.info("File name:" + fileName + ", Raw size:" + request().body().asRaw().size());

        try {
            String md5 = getRequestParam("md5");
            String md5File = DigestUtils.md5Hex(new FileInputStream(file));

            if(!md5File.equals(md5)){
                Logger.error("md5 do not match, from javascript: " +  md5 + ", server side md5: " + md5File);

            } else {
                Logger.info("md5 match, from javascript: " +  md5 + ", server side md5: " + md5File);

            }
            IOUtils.copy(new FileInputStream(file), new FileOutputStream(newFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ok("{\"success\":\"true\", \"fileName\":\"" + fileName
                + "\"}");
    }

    public static Result uploadForm() {
        MultipartFormData body = request().body().asMultipartFormData();
        FilePart picture = body.getFile("picture");
        String md5 = body.asFormUrlEncoded().get("md5")[0];
        if (picture != null) {
            String fileName = picture.getFilename();
            String contentType = picture.getContentType();
            File file = picture.getFile();
            try {
                String md5File = DigestUtils.md5Hex(new FileInputStream(file));
                if(!md5File.equals(md5)){
                    Logger.error("md5 do not match, from javascript: " +  md5 + ", server side md5: " + md5File);

                } else {
                    Logger.info("md5 match, from javascript: " +  md5 + ", server side md5: " + md5File);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return redirect(routes.Upload.renderUpload());
        } else {
            flash("error", "Missing file");
            return redirect(routes.Upload.index());
        }
    }

    public static Result uploadDirect() {
        File file = request().body().asRaw().asFile();
        return ok("File uploaded");
    }

    public static Result renderUpload() {
        return ok(formUpload.render("Your new application is ready."));
    }


    public static Result fetchDocument(String fileName) throws IOException {

        response().setHeader("Content-Disposition",
                "attachment;filename=\"" + fileName + "\"");
        return ok(new File(fileName));
    }

    protected static String getRequestParam(String key) {
        return request().queryString().containsKey(key) ? request()
                .queryString().get(key)[0] : null;
    }
}
