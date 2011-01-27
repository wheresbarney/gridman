package org.gridman.testtools.coherence.classloader;

import org.gridman.testtools.classloader.ClassloaderLifecycle;
import org.gridman.testtools.classloader.SystemPropertyLoader;

import java.util.*;

/**
 * A class to hold information about an isolated classloader cluster.
 *
 * @author <a href="jk@thegridman.com">Jonathan Knight</a>
 */
public class ClusterInfo<T extends ClassloaderLifecycle> {
    public static final String PROP_CLUSTER_PREFIX = "coherence.incubator.cluster.";
    public static final String PROP_DEFAULT_PROPERTIES = PROP_CLUSTER_PREFIX + "defaultProperties";
    public static final String PROP_SUFFIX_SERVERCLASS = ".server";
    public static final String PROP_SUFFIX_PROPERTIES = ".properties";
    public static final String PROP_SUFFIX_SERVER_COUNT = ".count";

    private String identifier;
    private Properties clusterProperties;
    private Map<Integer, Properties> localPropertyMap;
    private Map<Integer, Class<T>> serverClasses;

    private Map<Integer,ClusterNodeGroup> groups;
    private Map<Integer,Map<Integer,ClusterNode>> nodes;

    public ClusterInfo(String propertiesResource) {
        this(SystemPropertyLoader.loadProperties(propertiesResource));
    }

    public ClusterInfo(Properties properties) {
        this(properties.getProperty("cluster.name", UUID.randomUUID().toString()), properties);
    }

    public ClusterInfo(String identifier, String propertiesFile) {
        this(identifier, SystemPropertyLoader.loadProperties(propertiesFile));
    }

    public ClusterInfo(String identifier, Properties properties) {
        this.identifier = identifier;
        clusterProperties = properties;
        localPropertyMap = new HashMap<Integer, Properties>();
        serverClasses = new HashMap<Integer, Class<T>>();

        nodes = new HashMap<Integer,Map<Integer,ClusterNode>>();

        groups = new TreeMap<Integer,ClusterNodeGroup>();
        int groupId = 0;
        while (getServerClassName(groupId) != null) {
            ClusterNodeGroup group = new ClusterNodeGroup(this, groupId);
            groups.put(groupId, group);
            Map<Integer,ClusterNode> groupNodes = new TreeMap<Integer,ClusterNode>();
            nodes.put(groupId, groupNodes);
            String prefix = PROP_CLUSTER_PREFIX + groupId;
            int  nodeCount = Integer.parseInt(getProperty(prefix + PROP_SUFFIX_SERVER_COUNT));
            for (int i=0; i<nodeCount; i++) {
                groupNodes.put(i, new ClusterNode(group, i));
            }
            groupId++;
        }


    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getGroupCount() {
        return groups.size();
    }

    public Set<ClusterNodeGroup> getGroups() {
        return new TreeSet<ClusterNodeGroup>(groups.values());
    }

    public ClusterNodeGroup getGroup(int groupId) {
        return groups.get(groupId);
    }

    public Set<ClusterNode> getNodesForGroup(int groupId) {
        Set<ClusterNode> groupNodes;
        if (nodes.containsKey(groupId)) {
            groupNodes = new TreeSet<ClusterNode>(nodes.get(groupId).values());
        } else {
            groupNodes = Collections.emptySet();
        }
        return groupNodes;
    }

    public ClusterNode getNode(ClusterNodeGroup group, int nodeId) {
        return getNode(group.getGroupId(), nodeId);
    }

    public ClusterNode getNode(int groupId, int nodeId) {
        ClusterNode node = null;
        if (nodes.containsKey(groupId)) {
            node = nodes.get(groupId).get(nodeId);
        }
        return node;
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
    public Class<T> getServerClass(int groupId) {
        if (!serverClasses.containsKey(groupId)) {
            String serverClassName = getServerClassName(groupId);
            if (serverClassName == null) {
                throw new IllegalArgumentException("Server class property (" +
                        PROP_CLUSTER_PREFIX + groupId + PROP_SUFFIX_SERVERCLASS + "must not be blank");
            }

            Class<T> serverClass;
            try {
                serverClass = (Class<T>) Class.forName(serverClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Server Class Not Found: " + serverClassName);
            }

            if (!ClassloaderLifecycle.class.isAssignableFrom(serverClass)) {
                throw new ClassCastException(serverClass.getCanonicalName() + " class should implement " + ClassloaderLifecycle.class.getCanonicalName());
            }

            serverClasses.put(groupId, serverClass);
        }

        return serverClasses.get(groupId);
    }

    public Properties getLocalProperties(int groupId) {
        if (!localPropertyMap.containsKey(groupId)) {
            String defaultProperties = getProperty(PROP_DEFAULT_PROPERTIES);
            Properties localProperties = SystemPropertyLoader.loadProperties(defaultProperties);

            String prefix = PROP_CLUSTER_PREFIX + groupId;
            String localPropertiesList = getProperty(prefix + PROP_SUFFIX_PROPERTIES);
            String[] localPropertiesNames = localPropertiesList != null ? localPropertiesList.split(",") : new String[0];
            SystemPropertyLoader.addProperties(localProperties, localPropertiesNames);

            int argsCounter = 0;

            while(true) {
                String argPrefix = prefix + ".args." + argsCounter++;
                String key = getProperty(argPrefix + ".key");
                if (key == null) { break; }
                String value = getProperty(argPrefix + ".value");
                localProperties.setProperty(key, value);
            }
            localPropertyMap.put(groupId, localProperties);
        }

        return localPropertyMap.get(groupId);
    }
}
