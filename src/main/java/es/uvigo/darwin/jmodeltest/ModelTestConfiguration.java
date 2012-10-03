package es.uvigo.darwin.jmodeltest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class ModelTestConfiguration {

	 /** The application APPLICATION_PROPERTIES. */
    private static final Properties APPLICATION_PROPERTIES = new Properties();;
    
    public static final String DEFAULT_EXE_DIR = "exe/phyml";
    public static final String DEFAULT_LOG_DIR = "log";
    public static final boolean DEFAULT_GLOBAL_PHYML = false;
    
    public static final String AUTO_LOG = "auto-logging";
    public static final String LOG_DIR = "log-dir";
    public static final String EXE_DIR = "exe-dir";
    public static final String GLOBAL_PHYML_EXE = "global-phyml-exe";
    public static final String G_THREADS = "gamma-threads";
    public static final String I_THREADS = "inv-threads";
    public static final String U_THREADS = "uniform-threads";
    
    //TODO unique config-file for each execution
    public static void setConfigFile(String configFile) 
    {
        try 
        {
            FileInputStream prop = new FileInputStream(configFile);
            APPLICATION_PROPERTIES.load(prop);
        } catch (IOException e) {
            System.err.println("Configuration file (" + configFile + " cannot be resolved");
            System.exit(-1);
        }
    }
    
    public static String getProperty(String key) {
    	return APPLICATION_PROPERTIES.getProperty(key);
    }
    
    public static String getExeDir() {
    	String exeDir = getProperty(EXE_DIR);
    	return exeDir != null?exeDir : DEFAULT_EXE_DIR;
    }
        
    public static boolean isGlobalPhymlBinary() {
    	String propValue = getProperty(GLOBAL_PHYML_EXE); 
    	return (propValue != null && propValue.equalsIgnoreCase("true"));
    }
    
    public static boolean isAutoLogEnabled() {
    	return getProperty(AUTO_LOG).equalsIgnoreCase("enabled");
    }
    
    public static String getLogDir() {
    	String logDir = getProperty(LOG_DIR);
    	return logDir != null?logDir : DEFAULT_LOG_DIR;
    }
    
    public static Properties getProperties() {
    	return APPLICATION_PROPERTIES;
    }
    
}
