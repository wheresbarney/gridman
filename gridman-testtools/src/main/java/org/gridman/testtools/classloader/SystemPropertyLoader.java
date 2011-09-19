package org.gridman.testtools.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Simple System Property loader.
 *
 * @author Andrew Wilson
 * @author <a href="jk@thegridman.com">Jonathan Knight</a>
 */
public class SystemPropertyLoader {
    private static final Logger logger = LoggerFactory.getLogger(SystemPropertyLoader.class);
    
    public static void loadSystemProperties(String... resourceNames) {
        addProperties(System.getProperties(), resourceNames);
    }

    public static Properties loadCommaDelimitedPropertiesResourceList(String resourceList) {
        Properties properties = new Properties();
        String[] resourceNames = resourceList.split(",");
        addProperties(properties, resourceNames);
        return properties;
    }

    public static Properties loadProperties(String... resourceNames) {
        Properties properties = new Properties();
        addProperties(properties, resourceNames);
        return properties;
    }

    public static void addProperties(Properties properties, String... resourceNames) {
        for (String resourceName : resourceNames) {
            if (resourceName != null && resourceName.length() > 0) {
                Properties propertiesToLoad = getSystemProperties(resourceName);
                for (Object key : propertiesToLoad.keySet()) {
                    String value = propertiesToLoad.getProperty((String) key);
                    properties.setProperty((String)key, value);
                }
            }
        }
    }

    public static String getRequiredProperty(String property) {
        String value = System.getProperty(property);
        if(value==null) { throw new IllegalArgumentException("Required system property : " + property); }
        return value;
    }

    public static int getRequiredInteger(String property) {
        return Integer.parseInt(getRequiredProperty(property));
    }

    public static void loadEnvironment(String env) {
        addProperties(System.getProperties(), env);
    }

    public static Properties getSystemProperties(String resourceName) {
        try {
            InputStream stream = SystemPropertyLoader.class.getResourceAsStream(resourceName);
            if(stream == null) { throw new RuntimeException("Stream is null : " + resourceName); }
            Properties properties = new Properties();
            properties.load(stream);
            stream.close();

            String includes = properties.getProperty("gridman.include");
            if (includes != null && includes.length() > 0) {
                Properties included = loadCommaDelimitedPropertiesResourceList(includes);
                for (String name : included.stringPropertyNames()) {
                    if (!properties.containsKey(name)) {
                        properties.setProperty(name, included.getProperty(name));
                    }
                }
            }

            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties " + resourceName, e);
        }
    }

}
