package controllers;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;


import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import util.DocxService;
import views.html.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import settings.Constant;

public class Upload extends Controller {

	public static Result uploadDocument(String fileName,String type) throws Docx4JException {
		File file = request().body().asRaw().asFile();
		String myUploadPath = Constant.USER_DIR;
		File newFile = new File(myUploadPath, fileName);
		Logger.info("File name:" + fileName + ", Raw size:"
				+ request().body().asRaw().size());

		try {
			String md5 = getRequestParam("md5");
			String md5File = DigestUtils.md5Hex(new FileInputStream(file));

			if (!md5File.equals(md5)) {
				Logger.error("md5 do not match, from javascript: " + md5
						+ ", server side md5: " + md5File);

			} else {
				Logger.info("md5 match, from javascript: " + md5
						+ ", server side md5: " + md5File);

			}
			IOUtils.copy(new FileInputStream(file), new FileOutputStream(
					newFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(type.equals("doc")){
			util.DocxService.parseDocument(
					DocxService.DocType.DOC, fileName, myUploadPath + "/"
							+ fileName);
		}
		else if(type.equals("module")){
			util.DocxService.parseDocument(
					DocxService.DocType.MODULE, fileName, myUploadPath + "/"
							+ fileName);
		}
		
		return ok("{\"success\":\"true\", \"fileName\":\"" + fileName + "\"}");
	}

	public static Result uploadForm() {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picture = body.getFile("picture");
		String md5 = body.asFormUrlEncoded().get("md5")[0];
		if (picture != null) {
			@SuppressWarnings("unused")
			String fileName = picture.getFilename();
			@SuppressWarnings("unused")
			String contentType = picture.getContentType();
			File file = picture.getFile();
			try {
				String md5File = DigestUtils.md5Hex(new FileInputStream(file));
				if (!md5File.equals(md5)) {
					Logger.error("md5 do not match, from javascript: " + md5
							+ ", server side md5: " + md5File);

				} else {
					Logger.info("md5 match, from javascript: " + md5
							+ ", server side md5: " + md5File);

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return redirect(routes.Upload.renderUpload());
		} else {
			flash("error", "Missing file");
			return redirect(routes.Documents.view());
		}
	}

	public static Result uploadDirect() {
		@SuppressWarnings("unused")
		File file = request().body().asRaw().asFile();
		return ok("File uploaded");
	}

	public static Result renderUpload() {
		return ok(formUpload.render("Your new application is ready."));
	}

	public static Result fetchDocument(String fileName) throws IOException,
			Docx4JException {

		response().setHeader("Content-Disposition",
				"attachment;filename=\"" + fileName + "\"");
		if (util.DocxService.getCompiledModule(fileName)) {
			return ok(new File(Constant.USER_DIR + "/" + fileName + ".docx"));
		} else {
			return badRequest();
		}

	}

	protected static String getRequestParam(String key) {
		return request().queryString().containsKey(key) ? request()
				.queryString().get(key)[0] : null;
	}
}
