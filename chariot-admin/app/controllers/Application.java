package controllers;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.Comments.Comment;

import com.aperture.docx.core.Docx;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import models.*;
import views.html.*;

public class Application extends Controller {

	// -- Authentication

	public static class Login {

		public String email;
		public String password;

		public String validate() {
			if (User.authenticate(email, password) == null) {
				return "Invalid user or password";
			}
			return null;
		}

	}

	/**
		* Login page.
		*/
	public static Result login() {
		return ok(login.render(form(Login.class)));
	}

	/**
		* Handle login form submission.
		*/
	public static Result authenticate() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			return badRequest(login.render(loginForm));
		} else {
			session("email", loginForm.get().email);
			return redirect(routes.Documents.view());
		}
	}

	/**
		* Logout and clean the session.
		*/
	public static Result logout() {
		session().clear();
		flash("success", "You've been logged out");
		return redirect(routes.Application.login());
	}

	// -- Javascript routing

	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(Routes.javascriptRouter("jsRoutes",
			// Routes for Documents
		controllers.routes.javascript.Documents.index()));
	}

	/***********************************************
		* test stub by Aohajin *
		***********************************************
		*/	
		private static Result showDocStructure(java.io.InputStream in) throws  Docx4JException, 
	UnsupportedEncodingException {
		// 
		final StringBuilder sb = new StringBuilder();	
		final WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
			.load(in);
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart
			.getJaxbElement();
		Body body = wmlDocumentEl.getBody();

		new TraversalUtil(body, new Callback() {
			String indent = "";

			@Override
			public List<Object> apply(Object o) {
				String text = "";
				if (o instanceof org.docx4j.wml.P) {
					// ((org.docx4j.wml.P)o);
				}
				if (o instanceof org.docx4j.wml.Text)
					text = ((org.docx4j.wml.Text) o).getValue();
				else if (o instanceof org.docx4j.wml.R.CommentReference) {
					org.docx4j.wml.R.CommentReference cr = (org.docx4j.wml.R.CommentReference) o;
					for (Comment c : wordMLPackage.getMainDocumentPart()
					.getCommentsPart().getContents().getComment()) {
						if (c.getId().equals(cr.getId())){
							text = Docx.extractText(c);
							break;
						}
					}
				}

				sb.append(indent + o.getClass().getName() + "  \"" + text
					+ "\"\n");
				// sb.append(text+"\n");
				// System.out.println();
				return null;
			}

			@Override
			public boolean shouldTraverse(Object o) {
				if (o instanceof org.docx4j.wml.Br
					|| o instanceof org.docx4j.wml.R.Tab
					|| o instanceof org.docx4j.wml.R.LastRenderedPageBreak) {
					return false;
				}
				return true;
			}

			// Depth first
			@Override
			public void walkJAXBElements(Object parent) {
				indent += "    ";
				List<Object> children = getChildren(parent);
				if (children != null) {
					for (Object o : children) {
						// if its wrapped in javax.xml.bind.JAXBElement, get its
						// value
						o = XmlUtils.unwrap(o);
						this.apply(o);
						if (this.shouldTraverse(o)) {
							walkJAXBElements(o);
						}
					}
				}
				indent = indent.substring(0, indent.length() - 4);
			}

			@Override
			public List<Object> getChildren(Object o) {
				return TraversalUtil.getChildrenImpl(o);
			}
		});
		return ok(sb.toString());				
	}
			
	public static Result parse(String name) throws Docx4JException,
	java.io.FileNotFoundException,UnsupportedEncodingException {
		/*
			* String path = settings.Constant.DEBUG_PATH + "/" + "sample.docx";
			* 
			* Docx doc = new Docx(path); new ModuleParser(doc).parseAs("sample");
			* 
			* // test doc gen ModuleCompiler mc = new ModuleCompiler();
			* com.aperture.docx.templating.Module m = new
			* com.aperture.docx.templating.Module(); m.init("sample");
			* mc.pendModule(m); mc.save(settings.Constant.DEBUG_PATH + "/" +
			* "compiled.docx");
			*/

			// ByteArrayOutputStream
		String path = settings.Constant.DEBUG_PATH + "/" + name + ".docx";
		
		return showDocStructure(new FileInputStream(path));
	}

	public static Result all(String name) throws Docx4JException,
	UnsupportedEncodingException {
		name = URLDecoder.decode(name, "utf-8");
		models.Module module = models.Module.find.select("id, name, gfsId")
			.where().eq("name", name).findUnique();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		com.aperture.docx.scala.Gfs.load(module.gfsId, out);

		return showDocStructure(new ByteArrayInputStream(out.toByteArray()));
	}
	
	public static Result test() {
		return ok(request().host());
	}
}
