package es.uvigo.darwin.jmodeltest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public abstract class ModelTestConfiguration {

	private static String convertPathToAbsolute(String path) {
		Map<String, String> envMap = System.getenv();
    	String pattern = "\\$\\{([A-Za-z0-9]+)\\}";
    	Pattern expr = Pattern.compile(pattern);
    	Matcher matcher = expr.matcher(path);
    	while (matcher.find()) {
    	    String envValue = envMap.get(matcher.group(1).toUpperCase());
    	    if (envValue == null) {
    	        envValue = "";
    	    } else {
    	        envValue = envValue.replace("\\", "\\\\");
    	    }
    	    Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
    	    path = subexpr.matcher(path).replaceAll(envValue);
    	}
    	
		if (Utilities.isWindows()) {
			// change wrong path separators
			path.replace('/', '\\');
		} else {
			File fPath = new File(path);
			if (!fPath.isAbsolute()) {
				path = PATH + path;
			}
		}
		return path;
	}
	
	 /** The application APPLICATION_PROPERTIES. */
    private static final Properties APPLICATION_PROPERTIES;
    
    public static final boolean DEFAULT_GLOBAL_PHYML = false;
    
    private static String JAR_PATH;
    public static String PATH;
    public static String DEFAULT_EXE_DIR;
    public static String DEFAULT_LOG_DIR;
    
    public static final String HTML_LOG = "html-logging";
    public static final String PHYML_LOG = "phyml-logging";
    public static final String CKP_LOG = "checkpointing";
    public static final String LOG_DIR = "log-dir";
    public static final String EXE_DIR = "exe-dir";
    public static final String GLOBAL_PHYML_EXE = "global-phyml-exe";
    public static final String G_THREADS = "gamma-threads";
    public static final String I_THREADS = "inv-threads";
    public static final String U_THREADS = "uniform-threads";
    
    static {
        APPLICATION_PROPERTIES = new Properties();
        
        try {
        	try {
        		File f = new File(ModelTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        		JAR_PATH = f.getAbsolutePath();
        		PATH = JAR_PATH.replaceFirst(new File(JAR_PATH).getName(),"");
        		DEFAULT_EXE_DIR = PATH + "exe" + File.separator + "phyml";
        		DEFAULT_LOG_DIR = PATH + "log";
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	FileInputStream prop = new FileInputStream(convertPathToAbsolute(ModelTest.CONFIG_FILE));
            APPLICATION_PROPERTIES.load(prop);
            /* load also user definitions */
            for (Object key : ModelTest.USERDEF_PROPERTIES.keySet()) {
            	String strKey = (String) key;
            	if (strKey.equals(LOG_DIR) || strKey.equals(EXE_DIR)) {
            		File fDir = new File(ModelTest.USERDEF_PROPERTIES.getProperty(strKey));
            		if (!fDir.exists()) {
            			System.err.println("\nCOMMAND LINE ERROR: Unexistent directory " + fDir.getAbsolutePath());
            			ModelTest.CommandLineError();
            		} else if (!(fDir.isDirectory() && fDir.canRead()) || (strKey.equals(LOG_DIR) && !fDir.canWrite())) {
            			System.err.println("\nCOMMAND LINE ERROR: Argument " + fDir.getAbsolutePath() 
            					+ " is not a directory or you have not the required permissions on it");
            			ModelTest.CommandLineError();
        			}
            		APPLICATION_PROPERTIES.setProperty(strKey, fDir.getAbsolutePath());
            	} else {
            		APPLICATION_PROPERTIES.setProperty(strKey, ModelTest.USERDEF_PROPERTIES.getProperty(strKey));
            	}
            }
            
        	if (!existsKey(LOG_DIR)) {
        		APPLICATION_PROPERTIES.setProperty(HTML_LOG, "disabled");
        		APPLICATION_PROPERTIES.setProperty(PHYML_LOG, "disabled");
        		APPLICATION_PROPERTIES.setProperty(CKP_LOG, "disabled");
        	}
        	
        } catch (IOException e) {
            System.err.println("Configuration file ("+ convertPathToAbsolute(ModelTest.CONFIG_FILE) +") cannot be resolved");
            System.exit(-1);
        }
    }
    
    public static boolean existsKey(String key) {
	return !(getProperty(key).equals("n/a"));
    }

    public static String getProperty(String key) {
    	return APPLICATION_PROPERTIES.getProperty(key, "n/a");
    }
    
    public static String getExeDir() {
    	String exeDir;
		if (existsKey(EXE_DIR)) {
			exeDir = getProperty(EXE_DIR);
		} else {
			exeDir = DEFAULT_EXE_DIR;
		}
		return convertPathToAbsolute(exeDir);
	}
    
    public static void disableCkpLog() {
    	APPLICATION_PROPERTIES.setProperty(CKP_LOG, "disabled");
    }
    
    public static void disablePhymlLog() {
    	APPLICATION_PROPERTIES.setProperty(PHYML_LOG, "disabled");
    }
    
    public static void disableHtmlLog() {
    	APPLICATION_PROPERTIES.setProperty(HTML_LOG, "disabled");
    }
    
    public static boolean isGlobalPhymlBinary() {
    	String propValue = getProperty(GLOBAL_PHYML_EXE); 
    	return (existsKey(propValue) && getProperty(GLOBAL_PHYML_EXE).equalsIgnoreCase("true"));
    }
    
    public static boolean isHtmlLogEnabled() {
    	return getProperty(HTML_LOG).equalsIgnoreCase("enabled");
    }
    
    public static boolean isPhymlLogEnabled() {
    	return getProperty(PHYML_LOG).equalsIgnoreCase("enabled");
    }
    
    public static boolean isCkpEnabled() {
    	return getProperty(CKP_LOG).equalsIgnoreCase("enabled");
    }
    
    public static String getLogDir() {
    	String logDir = getProperty(LOG_DIR);
    	if (logDir == null) {
    		logDir = DEFAULT_LOG_DIR;
    	}
    	
    	return convertPathToAbsolute(logDir);
    }
    
    public static Properties getProperties() {
    	return APPLICATION_PROPERTIES;
    }
    
}
