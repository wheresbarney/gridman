package org.gridman.classloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ClusterProperties {

	private final Properties properties = new Properties();
	private final List<ClusterNodeProperties> clusterNodes = new ArrayList<ClusterNodeProperties>();

	public ClusterNodeProperties addNodeWithServers(int numberOfServers, Class<? extends ClassloaderLifecycle> clazz) {
		ClusterNodeProperties clusterNode = new ClusterNodeProperties(numberOfServers, clazz);
		clusterNodes.add(clusterNode);
		return clusterNode;
	}

	public Properties asProperties() {
		for (ClusterNodeProperties clusterNode : clusterNodes) {
			int memberIndex = clusterNodes.indexOf(clusterNode);
			properties.setProperty("coherence.incubator.cluster." + memberIndex + ".server", clusterNode.getFullyQualifiedClassName());
			properties.setProperty("coherence.incubator.cluster." + memberIndex + ".count", String.valueOf(clusterNode.getNumberOfServersToStart()));

			Properties nodeProperties = clusterNode.asProperties();
			List<String> propertyNames = new ArrayList<String>(nodeProperties.stringPropertyNames());

			for (String name : propertyNames) {
				int propertyIndex = propertyNames.indexOf(name);
				properties.setProperty("coherence.incubator.cluster." + memberIndex + ".args." + propertyIndex + ".key", name);
				properties.setProperty("coherence.incubator.cluster." + memberIndex + ".args." + propertyIndex + ".value", nodeProperties.getProperty(name));
			}
		}
		return properties;
	}
}
