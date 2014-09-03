package es.uvigo.darwin.jmodeltest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public abstract class ModelTestConfiguration {

	private static String convertPathToAbsolute(String path) {
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
    
    private static final String JAR_PATH = ModelTest.class.getProtectionDomain().getCodeSource().getLocation().getFile()
    		.replace("%20", " ");
    public static final String PATH = JAR_PATH.replaceFirst(new File(JAR_PATH).getName(),"");
    public static final String DEFAULT_EXE_DIR = PATH + "exe" + File.separator + "phyml";
    public static final String DEFAULT_LOG_DIR = PATH + "log";
    
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
            
            String logDir = getProperty(LOG_DIR);
        	if (logDir == null) {
        		APPLICATION_PROPERTIES.setProperty(HTML_LOG, "disabled");
        		APPLICATION_PROPERTIES.setProperty(PHYML_LOG, "disabled");
        		APPLICATION_PROPERTIES.setProperty(CKP_LOG, "disabled");
        	}
        } catch (IOException e) {
            System.err.println("Configuration file ("+ convertPathToAbsolute(ModelTest.CONFIG_FILE) +") cannot be resolved");
            System.exit(-1);
        }
    }
    
    public static String getProperty(String key) {
    	return APPLICATION_PROPERTIES.getProperty(key);
    }
    
    public static String getExeDir() {
    	String exeDir = getProperty(EXE_DIR);
    	if (exeDir == null) {
    		exeDir = DEFAULT_EXE_DIR;
    	}
    	return convertPathToAbsolute(exeDir);
    }
        
    public static boolean isGlobalPhymlBinary() {
    	String propValue = getProperty(GLOBAL_PHYML_EXE); 
    	return (propValue != null && propValue.equalsIgnoreCase("true"));
    }
    
    public static boolean isHtmlLogEnabled() {
    	return getProperty(HTML_LOG).equalsIgnoreCase("enabled");
    }
    
    public static boolean isPhymlLogEnabled() {
    	return getProperty(PHYML_LOG).equalsIgnoreCase("enabled");
    }
    
    public static boolean isCkpEnabled() {
    	return !getProperty(CKP_LOG).equalsIgnoreCase("disabled");
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
