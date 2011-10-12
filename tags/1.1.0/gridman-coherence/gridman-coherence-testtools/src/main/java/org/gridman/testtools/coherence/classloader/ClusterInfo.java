package org.gridman.testtools.coherence.classloader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.gridman.testtools.classloader.ClassloaderLifecycle;
import org.gridman.testtools.classloader.SystemPropertyLoader;

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
    private Properties globalOverrides;
    private Map<Integer, Properties> groupProperties;
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
        globalOverrides = new Properties();
        groupProperties = new HashMap<Integer, Properties>();
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

    public void addNodeToGroup(ClusterNodeGroup group) {
        ClusterNodeGroup actualGroup = getGroup(group.getGroupId());
        if (actualGroup != null) {
            Map<Integer, ClusterNode> clusterNodeMap = nodes.get(group.getGroupId());
            int id = clusterNodeMap.size();
            clusterNodeMap.put(id, new ClusterNode(actualGroup, id));

            String prefix = PROP_CLUSTER_PREFIX + actualGroup.getGroupId();
            clusterProperties.setProperty(prefix + PROP_SUFFIX_SERVER_COUNT, String.valueOf(id));
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
    public Class<T> getServerClass(ClusterNodeGroup group) {
        int groupId = group.getGroupId();
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

    public ClusterInfo<T> overrideProperty(String key, String value) {
        globalOverrides.setProperty(key, value);
        return this;
    }

    public ClusterInfo<T> removeOverrideProperty(String key) {
        globalOverrides.remove(key);
        return this;
    }

    public Properties getGroupProperties(int groupId) {
        if (!groupProperties.containsKey(groupId)) {
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
            groupProperties.put(groupId, localProperties);
        }

        Properties props = new Properties();
        props.putAll(groupProperties.get(groupId));
        props.putAll(globalOverrides);
        return props;
    }
}
