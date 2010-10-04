package org.gridman.classloader;

import java.util.Properties;

public class ClusterNodeProperties {

	private final int numberOfServersToStart;
	private final String fullyQualifiedClassName;
	private final Properties properties = new Properties();

	public ClusterNodeProperties(int numberOfServersToStart, Class<? extends ClassloaderLifecycle> clazz) {
		this.fullyQualifiedClassName = clazz.getName();
		this.numberOfServersToStart = numberOfServersToStart;

		useDefaultClusterProperties();
		useDefaultMulticastProperties();
		useDefaultJmxProperties();
	}

	public int getNumberOfServersToStart() {
		return numberOfServersToStart;
	}

	public String getFullyQualifiedClassName() {
		return fullyQualifiedClassName;
	}

	private void useDefaultClusterProperties() {
		withPofEnabled(true);
		withCoherenceOverride("coherence/simple/simpleOverride.xml");
		withClusterIdentifier("anew-1");
		withLogLevel(9);
		withCoherenceExtendPort(8098);

		// we currently turn of the guard because otherwise it kills things
		properties.setProperty("tangosol.coherence.guard.timeout", "0");
	}

	private void useDefaultJmxProperties() {
		withRemoteManagement(true);
		withRemoteJmxOverSsl(false);
		withRemoteJmxAuthentication(false);
	}

	private void useDefaultMulticastProperties() {
		withClusterPort(8016);
		withMulticastPacketTimeToLive(0);
	}

	public ClusterNodeProperties withPofEnabled(boolean enabled) {
		properties.setProperty("tangosol.pof.enabled", Boolean.toString(enabled));
		return this;
	}

	public ClusterNodeProperties withCoherenceOverride(String xmlConfigFile) {
		properties.setProperty("tangosol.coherence.override", xmlConfigFile);
		return this;
	}

	public ClusterNodeProperties withLogLevel(int logLevel) {
//		properties.setProperty("tangosol.coherence.log", "log4j");
		properties.setProperty("tangosol.coherence.log.level", Integer.toString(logLevel));
		return this;
	}

	public ClusterNodeProperties withCoherenceExtendPort(int portNumber) {
		properties.setProperty("coherence.extend.port", Integer.toString(portNumber));
		return this;
	}

	public ClusterNodeProperties withUnicastAddress(String unicastIpAddress) {
		properties.setProperty("tangosol.coherence.localhost", unicastIpAddress);
		return this;
	}

	public ClusterNodeProperties withUnicastPort(int portNumber) {
		properties.setProperty("tangosol.coherence.localport", Integer.toString(portNumber));
		return this;
	}

	public ClusterNodeProperties withUnicastPortAutoAdjust(boolean autoAdjust) {
		properties.setProperty("tangosol.coherence.localport.adjust", Boolean.toString(autoAdjust));
		return this;
	}

	public ClusterNodeProperties withClusterIdentifier(String identifier) {
		properties.setProperty("tangosol.coherence.cluster", identifier);
		return this;
	}

	public ClusterNodeProperties withClusterAddress(String address) {
		properties.setProperty("tangosol.coherence.clusteraddress", address);
		return this;
	}

	public ClusterNodeProperties withClusterPort(int portNumber) {
		properties.setProperty("tangosol.coherence.clusterport", Integer.toString(portNumber));
		return this;
	}

	public ClusterNodeProperties withMulticastPacketTimeToLive(int timeToLive) {
		properties.setProperty("tangosol.coherence.ttl", Integer.toString(timeToLive));
		return this;
	}

	public ClusterNodeProperties withRemoteManagement(boolean enabled) {
		properties.setProperty("com.sun.management.jmxremote", Boolean.toString(enabled));
		properties.setProperty("tangosol.coherence.management.remote", Boolean.toString(enabled));
		return this;
	}

	public ClusterNodeProperties withRemoteJmxOverSsl(boolean enabled) {
		properties.setProperty("com.sun.management.jmxremote.ssl", Boolean.toString(enabled));
		return this;
	}

	public ClusterNodeProperties withRemoteJmxAuthentication(boolean enabled) {
		properties.setProperty("com.sun.management.jmxremote.authenticate", Boolean.toString(enabled));
		return this;
	}

	public void useDefaultClientProperties() {
		withCacheConfig("client.xml");
		withDistributedLocalStorage(false);
		withTcmpEnabled(false);
	}

	public ClusterNodeProperties useDefaultServerProperties() {
		withDataBackups(1);
		withCacheConfig("server.xml");
		withTcmpEnabled(true);
		withDistributedLocalStorage(true);
		withStorageEnabled(true);
		withProxyEnabled(false);
		return this;
	}

	public ClusterNodeProperties withDataBackups(int numberOfDataBackups) {
		properties.setProperty("tangosol.coherence.distributed.backupcount", Integer.toString(numberOfDataBackups));
		return this;
	}

	public ClusterNodeProperties withCacheConfig(String xmlConfigFile) {
		properties.setProperty("tangosol.coherence.cacheconfig", xmlConfigFile);
		return this;
	}

	public ClusterNodeProperties withDistributedLocalStorage(boolean enabled) {
		properties.setProperty("tangosol.coherence.distributed.localstorage", Boolean.toString(enabled));
		return this;
	}

	public ClusterNodeProperties withStorageEnabled(boolean enabled) {
		properties.setProperty("coherence.isStorage", Boolean.toString(enabled));
		return this;
	}

	public ClusterNodeProperties withTcmpEnabled(boolean enabled) {
		properties.setProperty("tangosol.coherence.tcmp.enabled", Boolean.toString(enabled));
		return this;
	}

	public ClusterNodeProperties withProxyEnabled(boolean enabled) {
		properties.setProperty("coherence.isProxy", Boolean.toString(enabled));
		return this;
	}

	public Properties asProperties() {
		return properties;
	}
}
