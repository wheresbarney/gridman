package org.gridman.classloader;

import com.tangosol.util.Base;
import com.tangosol.util.LongArray;
import com.tangosol.util.SimpleLongArray;
import org.apache.log4j.Logger;
import org.gridman.classloader.coherence.CoherenceClassloaderLifecycle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class that will start a Coherence pseudo-cluster in a single JVM.
 * </p>
 * Each member of the cluster is isolated within its own Classloader.
 * System properties for each member are also loaded and isolated to that member.
 *
 * @author Andrew Wilson
 * @author <a href="jk@thegridman.com">Jonathan Knight</a>
 */
public class ClusterStarter extends Base {
    private static final Logger logger = Logger.getLogger(ClusterStarter.class);

    private static ClusterStarter sInstance;

    private Map<String, ClusterInfo> clusters;
    private Map<String, LongArray> services;
    
    private static final String CLUSTER_PREFIX = "coherence.incubator.cluster.";

    public static void main(String[] args) {
        new ClusterStarter().ensureCluster(args[0]);
    }

    private ClusterStarter() {
        clusters = new HashMap<String, ClusterInfo>();
        services = new HashMap<String, LongArray>();
    }

    /**
     * Returns the singleton instance of ClusterStarter
     * @return the singleton instance of ClusterStarter
     */
    public static synchronized ClusterStarter getInstance() {
        if(sInstance == null) {
            sInstance = new ClusterStarter();
        }
        return sInstance;
    }

    /**
     * Ensure all the servers in all the server groups of the specified
     * cluster file are started.
     *
     * @param clusterFile - the properties file for the cluster to ensure
     */
    public void ensureCluster(String clusterFile) {
        ClusterInfo clusterInfo = getClusterInfo(clusterFile);

        for (int groupId=0; groupId<clusterInfo.getGroupCount(); groupId++) {
            startAllServersInGroupInternal(clusterFile, groupId, false);
        }

        // Wait for the Services to come up...
        visitAllServices(clusterFile, new ServiceStartupWaitVisitor());
    }

    /**
     * Ensure all the servers in the specified group within the specified
     * cluster properties file are started.
     *
     * @param clusterFile - the properties file for the cluster
     * @param groupId - the group of servers to start
     */
    public void ensureAllServersInClusterGroup(String clusterFile, int groupId) {
        startAllServersInGroupInternal(clusterFile, groupId, true);
    }

    private void startAllServersInGroupInternal(String filename, int groupId, boolean waitForStart) {
        ClusterInfo clusterInfo = getClusterInfo(filename);
        if (groupId < clusterInfo.getGroupCount()) {
            for (int instance=0; instance<clusterInfo.getServerCount(groupId); instance++) {
                startServiceInstanceInternal(filename, groupId, instance, false);
            }
        }

        if (waitForStart) {
            // Wait for the Services to come up...
            visitAllServices(filename, new ServiceStartupWaitVisitor());
        }
    }

    /**
     * Ensure all the specified server instance within the specified group within the specified
     * cluster properties file is started.
     *
     * @param clusterFile - the properties file for the cluster
     * @param groupId - the group of the servers to start belongs to
     * @param instanceId - the instance of the server to start
     */
    public void ensureServerInstance(String clusterFile, int groupId, int instanceId) {
        startServiceInstanceInternal(clusterFile, groupId, instanceId, true);
    }

    private void startServiceInstanceInternal(String clusterFile, int groupId, int instanceId, boolean waitForStart) {
        LongArray serviceList = getServiceList(clusterFile, groupId);
        if (!serviceList.exists(instanceId)) {
            ClusterInfo clusterInfo = getClusterInfo(clusterFile);
            Class<? extends CoherenceClassloaderLifecycle> serverClass = clusterInfo.getServerClass(groupId);
            Properties localProperties = clusterInfo.getLocalProperties(groupId);

            ClassloaderRunner runner;
            try {
                runner = new ClassloaderRunner(serverClass.getCanonicalName(), localProperties);
            } catch (Throwable throwable) {
                throw ensureRuntimeException(throwable, "Error starting server clusterFile=" + clusterFile +
                        " groupId=" + groupId + " instance=" + instanceId);
            }

            serviceList.set(instanceId, runner);
        }

        if (waitForStart) {
            // Wait for the Services to come up...
            visitAllServices(clusterFile, new ServiceStartupWaitVisitor());
        }
    }

    /**
     * Shutdown all the servers in the specified cluster properties file.
     * @param clusterFilename - the cluster properties file to use to identify the servers to shutdown
     */
    public void shutdown(String clusterFilename) {
        logger.info("Shutting down all services : " + clusterFilename);
        visitAllServices(clusterFilename, new ServiceShutdownVisitor());
        logger.info("Shut down all services : " + clusterFilename);
    }

    /**
     * Shutdown all the servers in the specified group within the specified cluster properties file.
     * @param clusterFilename - the cluster properties file to use to identify the servers to shutdown
     * @param groupId - the server group to shut down
     */
    public void shutdown(String clusterFilename, int groupId) {
        logger.info("Shutting down all services : cluster=" + clusterFilename + " groupId=" + groupId);
        visitAllServicesInGroup(clusterFilename, groupId, new ServiceShutdownVisitor());
        logger.info("Shut down all services : cluster=" + clusterFilename + " groupId=" + groupId);
    }

    /**
     * Shutdown the specified server instance in the specified group within the specified cluster properties file.
     * @param clusterFilename - the cluster properties file to use to identify the server to shutdown
     * @param groupId - the server group containing the server to shut down
     * @param instance - the server instance to shut down
     */
    public void shutdown(String clusterFilename, int groupId, int instance) {
        logger.info("Shutting down service : cluster=" + clusterFilename + " groupId=" + groupId + " instance=" + instance);
        visitService(clusterFilename, groupId, instance, new ServiceShutdownVisitor());
        logger.info("Shut down service : cluster=" + clusterFilename + " groupId=" + groupId + " instance=" + instance);
    }

    ClusterInfo getClusterInfo(String filename) {
        if (filename == null || filename.length() == 0) {
            throw new IllegalArgumentException("filename argument cannot be null or empty String and must specifiy a valid properties file");
        }

        if (!clusters.containsKey(filename)) {
            clusters.put(filename, new ClusterInfo(filename));
        }
        return clusters.get(filename);
    }

    LongArray getClusterServiceList(String filename) {
        if (!services.containsKey(filename)) {
            services.put(filename, new SimpleLongArray());
        }
        return services.get(filename);
    }

    LongArray getServiceList(String filename, int groupId) {
        LongArray clusterServiceList = getClusterServiceList(filename);
        if (!clusterServiceList.exists(groupId)) {
            clusterServiceList.set(groupId, new SimpleLongArray());
        }
        return (LongArray) clusterServiceList.get(groupId);
    }

    @SuppressWarnings({"unchecked"})
    void visitAllServices(String filename, ServiceVisitor visitor) {
        LongArray clusterServicesList = getClusterServiceList(filename);
        Iterator<LongArray> it = clusterServicesList.iterator();
        while (it.hasNext()) {
            LongArray serviceList = it.next();
            Iterator<ClassloaderRunner> serviceIterator = serviceList.iterator();
            while (serviceIterator.hasNext()) {
                ClassloaderRunner service = serviceIterator.next();
                if (visitor.visit(service)) {
                    serviceIterator.remove();
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    void visitAllServicesInGroup(String filename, int groupId, ServiceVisitor visitor) {
        LongArray clusterServicesList = getClusterServiceList(filename);
        LongArray serviceList = (LongArray) clusterServicesList.get(groupId);
        if (serviceList != null) {
            Iterator<ClassloaderRunner> serviceIterator = serviceList.iterator();
            while (serviceIterator.hasNext()) {
                ClassloaderRunner service = serviceIterator.next();
                if (visitor.visit(service)) {
                    serviceIterator.remove();
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    void visitService(String filename, int groupId, int instance, ServiceVisitor visitor) {
        LongArray clusterServicesList = getClusterServiceList(filename);
        LongArray serviceList = (LongArray) clusterServicesList.get(groupId);
        if (serviceList != null) {
            ClassloaderRunner service = (ClassloaderRunner) serviceList.get(instance);
            if (service != null) {
                if (visitor.visit(service)) {
                    serviceList.remove(instance);
                }
            }
        }
    }

    /**
     * Interface used by classes that can perform an operation on (visit)
     * ClassloaderRunner instances.
     */
    private static interface ServiceVisitor {
        /**
         * Perform an operation (visit) the specified ClassloaderRunner
         * @param service the ClassloaderRunner to visit
         * @return true if the service should be considered dead
         */
        boolean visit(ClassloaderRunner service);
    }

    /**
     * ServiceVisitor that waits for services to be started
     */
    private static class ServiceStartupWaitVisitor implements ServiceVisitor {
        /**
         * Wait until the specified ClassloaderRunner's isStarted method returns true
         * @param service the ClassloaderRunner to visit
         */
        @Override
        public boolean visit(ClassloaderRunner service) {
            try {
                while(!service.isStarted()) {
                    logger.debug("Waiting for " + service);
                    Thread.sleep(1000);
                }
                return false;
            } catch (Exception e) {
                throw Base.ensureRuntimeException(e, "Error while waiting for service to start - service=" + service);
            }
        }
    }

    /**
     * ServiceVisitor that shuts down services
     */
    private static class ServiceShutdownVisitor implements ServiceVisitor {
        /**
         * Shutdown the specified ClassloaderRunner
         * @param service the ClassloaderRunner to visit
         */
        @Override
        public boolean visit(ClassloaderRunner service) {
            try {
                logger.debug("Shutting down " + service);
                service.shutdown();
                return true;
            } catch (Exception e) {
                throw Base.ensureRuntimeException(e, "Error shutting down service " + service);
            }
        }
    }
}
