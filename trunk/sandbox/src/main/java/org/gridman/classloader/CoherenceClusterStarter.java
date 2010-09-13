package org.gridman.classloader;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 */
public class CoherenceClusterStarter {
    private static final Logger logger = Logger.getLogger(CoherenceClusterStarter.class);

    private String clusterFilename;
    private static CoherenceClusterStarter sInstance;
    private List<SandboxRunner> services = new ArrayList<SandboxRunner>();

    private static final String CLUSTER_PREFIX = "coherence.incubator.cluster.";

    public static void main(String[] args) throws Throwable {
        new CoherenceClusterStarter().setCluster(args[0]);
    }

    private CoherenceClusterStarter() throws Throwable {}

    public static synchronized CoherenceClusterStarter getInstance() throws Throwable {
        if(sInstance == null) {
            sInstance = new CoherenceClusterStarter();
        }
        return sInstance;
    }

    public void setCluster(String filename) throws Throwable {
        if(filename.equals(clusterFilename)) {
            // @todo do we want this opto
            logger.info("Already loaded : " + clusterFilename);
            return;
        }

        if(clusterFilename != null) {
            shutdown();
        }

        clusterFilename = filename;

        logger.info("Loading clusterFilename : " + clusterFilename);
        Properties clusterProperties = SystemPropertyLoader.getSystemProperties(clusterFilename);
        SystemPropertyLoader.loadEnvironment(clusterProperties.getProperty(CLUSTER_PREFIX + "defaultProperties"));
        int counter = 0;
        while(true) {
            String prefix = CLUSTER_PREFIX + counter;
            String serverName = clusterProperties.getProperty(prefix + ".server");
            if(serverName == null) { break; }

            // pull in any properties file
            String localProperties = clusterProperties.getProperty(prefix + ".properties");
            Properties localArgs = new Properties();
            if(localProperties != null) {
                localArgs = SystemPropertyLoader.getSystemProperties(localProperties);
            }

            // pull in the args
            int argsCounter = 0;
            while(true) {
                String argPrefix = prefix + ".args." + argsCounter++;
                String key = clusterProperties.getProperty(argPrefix + ".key");
                if(key == null) { break; }
                String value = clusterProperties.getProperty(argPrefix + ".value");
                localArgs.setProperty(key,value);
            }

            // start the servers
            if(!SandboxServer.class.isAssignableFrom(Class.forName(serverName))) {
                throw new Exception("Should implement " + serverName + " : " + SandboxServer.class);
            }
            
            int serverCount = Integer.parseInt(clusterProperties.getProperty(prefix + ".count"));
            logger.info("Starting " + serverCount + " : " + serverName + " : " + localArgs);
            for(int i=0;i<serverCount;i++) {
                services.add(new SandboxRunner(serverName, localArgs));
            }
            counter++;
        }

        // Wait for the Services to come up...
        logger.debug("Waiting for services");
        for(SandboxRunner service : services) {
            while(!service.isStarted()) {
                logger.debug("Waiting for " + service);
                Thread.sleep(1000);
            }
        }

        // setup the client properties
        SystemPropertyLoader.loadEnvironment(clusterProperties.getProperty(CLUSTER_PREFIX + "clientProperties"));
    }

    public void shutdown() throws Exception {
        logger.info("Shutting down cluster : " + clusterFilename);
        for (SandboxRunner service : services) {
            service.shutdown();
        }
        services.clear();
        logger.info("Shut down cluster : " + clusterFilename);
        clusterFilename = null;
    }
}
