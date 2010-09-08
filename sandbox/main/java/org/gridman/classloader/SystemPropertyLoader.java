package org.gridman.classloader;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * Simple System Property loader.
 */
public class SystemPropertyLoader {
    private static final Logger logger = Logger.getLogger(SystemPropertyLoader.class);
    
    public static final String MARKER_ENVIRONMENT = "coherence.incubator.environment";
    
    public static void addSystemProperties(String resourceName) throws Exception {
        Properties properties = getSystemProperties(resourceName);
        for (Object key : properties.keySet()) {
            String value = properties.getProperty((String) key);
            System.setProperty((String)key, value);
            logger.debug("Setting property : " + key + " : " + value);
        }
    }

    public static void loadProperties(String[] args) throws Exception {
        // Load the command line args
        if(args != null) {
            for(int i = 0;i<args.length;i+=2) {
                System.setProperty(args[i], args[i+1]);
            }
        }

        SystemPropertyLoader.addSystemProperties("/coherence/default.properties");
        SystemPropertyLoader.addSystemProperties(SystemPropertyLoader.getRequiredProperty(MARKER_ENVIRONMENT));
    }

    public static String getRequiredProperty(String property) throws Exception {
        String value = System.getProperty(property);
        if(value==null) { throw new Exception("Required system property : " + property); }
        return value;
    }

    public static int getRequiredInteger(String property) throws Exception{
        return Integer.parseInt(getRequiredProperty(property));
    }

    public static void loadEnvironment(String env) throws Exception {
        System.setProperty(MARKER_ENVIRONMENT,env);
        loadProperties(null);
    }

    public static Properties getSystemProperties(String resourceName) throws Exception {
        InputStream stream = SystemPropertyLoader.class.getResourceAsStream(resourceName);
        if(stream == null) { throw new RuntimeException("Stream is null : " + resourceName); }
        Properties properties = new Properties();
        properties.load(stream);
        stream.close();
        return properties;
    }
}
