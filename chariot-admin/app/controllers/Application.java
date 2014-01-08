package controllers;

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

import com.aperture.docx.Docx;
import com.aperture.docx.dom.DocxTreeStructure;
import com.aperture.docx.dom.ModuleCompiler;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import models.*;
import util.DocxService;
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
	 ***********************************************/
	public static Result parse() throws Docx4JException {
		String path = settings.Constant.DEBUG_PATH + "/" + "sample.docx";

		Docx doc = new Docx(path);
		new DocxTreeStructure(doc).parseAs("sample");

		// test doc gen
		ModuleCompiler mc = new ModuleCompiler();
		com.aperture.docx.dom.Module m = new com.aperture.docx.dom.Module();
		m.init("sample");
		mc.pendModule(m);
		mc.save(settings.Constant.DEBUG_PATH + "/" + "compiled.docx");

		return ok("done!");
	}

	private static void iter(List<Object> list, StringBuilder sb, String indent) {
		for (Object o : list) {
			sb.append(indent + o.getClass().getName() + "\n");
			if (o instanceof models.template.Module) {
				iter(((models.template.Module) o).list, sb, indent + "\t");
			}
		}
	}

	public static Result analyze() throws Docx4JException {
		StringBuilder sb = new StringBuilder();
		iter(DocxService.analyzeModule("sample").list, sb, "");
		return ok(sb.toString());
	}

	public static Result all(String name) throws Docx4JException, UnsupportedEncodingException {
		name = URLDecoder.decode(name, "utf-8");
		String inputfilepath = settings.Constant.DEBUG_PATH + "/" + name
				+ ".docx";

		final StringBuilder sb = new StringBuilder();

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
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
				sb.append(indent + o.getClass().getName() + "  \"" + text
						+ "\"\n");
				// sb.append(text+"\n");
				// System.out.println();
				return null;
			}

			@Override
			public boolean shouldTraverse(Object o) {
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
}
