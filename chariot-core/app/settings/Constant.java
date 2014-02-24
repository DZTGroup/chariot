package settings;

public class Constant {
	final public static int QUESTION_CONTEXT_RADIUS = 20;
	final public static String QUESTON_BLANK_REPRESENTATION = "<#Question>";
	
	final public static String DOCX_TEMPLATE = System.getProperty("user.dir") + "/template/template.docx";
	final public static String DEBUG_PATH = System.getProperty("user.dir") + "/template/debug";
	final public static String MODULE_PATH = System.getProperty("user.dir") + "/template/module";
	
	final public static String WEBSITE_ADDRESS="http://localhost:9001";
	
	
	final public static String SYSTEM_AUTHOR = "system";
	
	//System.getProperty("user.dir")+"/userdir";
	final public static String USER_DIR = "/tmp";
	
	// depending on os
	final private static String LIBRE_OFFICE_MAC = "/Applications/LibreOffice.app/Contents/MacOS/soffice";
	final private static String LIBRE_OFFICE_UNIX = "libreoffice";
	
	final public static String LIBRE_OFFICE;
	static{
		String OS = System.getProperty("os.name").toLowerCase();
		if ( OS.indexOf("mac") >= 0 ){
			LIBRE_OFFICE = LIBRE_OFFICE_MAC;
		} else if ( OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") >= 0){
			LIBRE_OFFICE = LIBRE_OFFICE_UNIX;
		} else {
			LIBRE_OFFICE = null;
		}
	}
}
