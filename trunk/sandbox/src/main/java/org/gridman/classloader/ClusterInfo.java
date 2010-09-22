package org.gridman.classloader;

import org.gridman.classloader.coherence.CoherenceClassloaderLifecycle;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A class to hold information about an isolated classloader cluster.
 *
 * @author <a href="jk@thegridman.com">Jonathan Knight</a>
 */
public class ClusterInfo {
    public static final String PROP_CLUSTER_PREFIX = "coherence.incubator.cluster.";
    public static final String PROP_DEFAULT_PROPERTIES = PROP_CLUSTER_PREFIX + "defaultProperties";
    public static final String PROP_SUFFIX_SERVERCLASS = ".server";
    public static final String PROP_SUFFIX_PROPERTIES = ".properties";
    public static final String PROP_SUFFIX_SERVER_COUNT = ".count";

    private Properties clusterProperties;
    private Map<Integer, Properties> localPropertyMap;
    private Map<Integer, Class<? extends CoherenceClassloaderLifecycle>> serverClasses;

    private int groupCount;

    public ClusterInfo(String clusterFilename) {
        clusterProperties = SystemPropertyLoader.getSystemProperties(clusterFilename);
        localPropertyMap = new HashMap<Integer, Properties>();
        serverClasses = new HashMap<Integer, Class<? extends CoherenceClassloaderLifecycle>>();

        groupCount = 0;
        while (getServerClassName(groupCount) != null) {
            groupCount++;
        }
    }

    public int getGroupCount() {
        return groupCount;
    }

    public String getProperty(String propertyName) {
        return clusterProperties.getProperty(propertyName);
    }

    @SuppressWarnings({"unchecked"})
    public String getServerClassName(int groupId) {
        String prefix = PROP_CLUSTER_PREFIX + groupId;
        return getProperty(prefix + PROP_SUFFIX_SERVERCLASS);
    }

    @SuppressWarnings({"unchecked"})
    public Class<? extends CoherenceClassloaderLifecycle> getServerClass(int groupId) {
        if (!serverClasses.containsKey(groupId)) {
            String serverClassName = getServerClassName(groupId);
            if(serverClassName == null) {
                throw new IllegalArgumentException("Server class property (" +
                        PROP_CLUSTER_PREFIX + groupId + PROP_SUFFIX_SERVERCLASS + "must not be blank");
            }

            Class<? extends CoherenceClassloaderLifecycle> serverClass = null;
            try {
                serverClass = (Class<? extends CoherenceClassloaderLifecycle>) Class.forName(serverClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Server Class Not Found: " + serverClassName);
            }

            if(!CoherenceClassloaderLifecycle.class.isAssignableFrom(serverClass)) {
                throw new ClassCastException(serverClass.getCanonicalName() + " class should implement " + CoherenceClassloaderLifecycle.class.getCanonicalName());
            }

            serverClasses.put(groupId, serverClass);
        }

        return serverClasses.get(groupId);
    }

    public int getServerCount(int groupId) {
        String prefix = PROP_CLUSTER_PREFIX + groupId;
        return Integer.parseInt(getProperty(prefix + PROP_SUFFIX_SERVER_COUNT));
    }

    public Properties getLocalProperties(int groupId) {
        if (!localPropertyMap.containsKey(groupId)) {
            String defaultProperties = getProperty(PROP_DEFAULT_PROPERTIES);
            Properties localProperties = SystemPropertyLoader.loadProperties(defaultProperties);

            String prefix = PROP_CLUSTER_PREFIX + groupId;
            String localPropertiesList = getProperty(prefix + PROP_SUFFIX_PROPERTIES);
            String[] localPropertiesNames = (localPropertiesList != null) ? localPropertiesList.split(",") : new String[0];
            SystemPropertyLoader.addProperties(localProperties, localPropertiesNames);

            int argsCounter = 0;

            while(true) {
                String argPrefix = prefix + ".args." + argsCounter++;
                String key = getProperty(argPrefix + ".key");
                if(key == null) { break; }
                String value = getProperty(argPrefix + ".value");
                localProperties.setProperty(key, value);
            }
            localPropertyMap.put(groupId, localProperties);
        }
        
        return localPropertyMap.get(groupId);
    }
}
