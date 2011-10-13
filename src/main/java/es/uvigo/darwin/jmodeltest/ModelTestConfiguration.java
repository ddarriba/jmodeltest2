package es.uvigo.darwin.jmodeltest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class ModelTestConfiguration {

	 /** The application APPLICATION_PROPERTIES. */
    private static final Properties APPLICATION_PROPERTIES;
    
    public static final String AUTO_LOG = "auto-logging";
    public static final String LOG_DIR = "log-dir";
    public static final String EXE_DIR = "exe-dir";
    public static final String GLOBAL_PHYML_EXE = "global-phyml-exe";
    
    static {
        APPLICATION_PROPERTIES = new Properties();
        try {
            FileInputStream prop = new FileInputStream(ModelTest.CONFIG_FILE);
            APPLICATION_PROPERTIES.load(prop);
        } catch (IOException e) {
            System.err.println("Configuration file (conf/jmodeltest.conf) cannot be resolved");
            System.exit(-1);
        }
    }
    
    public static String getProperty(String key) {
    	return APPLICATION_PROPERTIES.getProperty(key);
    }
    
    public static boolean isAutoLogEnabled() {
    	return getProperty(AUTO_LOG).equalsIgnoreCase("enabled");
    }
    
}
