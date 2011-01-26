package org.gridman.testtools.coherence.classloader;

import com.tangosol.net.CacheFactory;
import com.tangosol.util.Base;
import com.tangosol.util.LongArray;
import com.tangosol.util.SimpleLongArray;
import org.gridman.testtools.classloader.ClassloaderLifecycle;
import org.gridman.testtools.classloader.ClassloaderRunner;
import org.gridman.testtools.classloader.SystemPropertyLoader;

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
    private static ClusterStarter sInstance;

    private Map<String, ClusterInfo> clusters;
    private Map<String, LongArray> services;
    private Map<String, Map<Integer,Properties>> servicePropertyOverrides;

    private Properties extraProperties;

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            new ClusterStarter().ensureCluster(args[0]);
        } else if (args.length == 2) {
            Properties props = SystemPropertyLoader.getSystemProperties(args[0]);
            int groupId = Integer.parseInt(args[1]);
            new ClusterStarter().startAllServersInGroupInternal(args[0], props, groupId, false);
        } else if (args.length == 3) {
            Properties props = SystemPropertyLoader.getSystemProperties(args[0]);
            int groupId = Integer.parseInt(args[1]);
            int instance = Integer.parseInt(args[2]);
            new ClusterStarter().startServiceInstanceInternal(args[0], props, groupId, instance, false);
        }

        final Object lock = new Object();
        synchronized (lock) {
            lock.wait();
        }
    }

    private ClusterStarter() {
        clusters = new HashMap<String, ClusterInfo>();
        services = new HashMap<String, LongArray>();
        servicePropertyOverrides = new HashMap<String, Map<Integer,Properties>>();
        extraProperties = new Properties();
    }

    public ClusterStarter setProperty(String key, String value) {
        extraProperties.setProperty(key, value);
        return this;
    }

    public ClusterStarter overrideClusterProperty(String identifier, int group, String key, String value) {
        getPropertyOverrides(identifier, group).setProperty(key, value);
        return this;
    }

    private Properties getPropertyOverrides(String identifier, int group) {
        if (!servicePropertyOverrides.containsKey(identifier)) {
            servicePropertyOverrides.put(identifier, new HashMap<Integer,Properties>());
        }
        Map<Integer,Properties> groupOverrides = servicePropertyOverrides.get(identifier);
        if (!groupOverrides.containsKey(group)) {
            groupOverrides.put(group, new Properties());
        }
        return groupOverrides.get(group);
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
        if (clusterFile == null || clusterFile.length() == 0) {
            throw new IllegalArgumentException("clusterFile argument cannot be null or empty String and must specifiy a valid properties file");
        }
        ensureCluster(clusterFile, SystemPropertyLoader.getSystemProperties(clusterFile));
        CacheFactory.shutdown();
    }

    /**
     * Ensure all the servers in all the server groups of the specified
     * cluster properties are started.
     *
     * @param identifier - an identifier for the cluster
     * @param properties - the properties for the cluster to ensure
     */
    public void ensureCluster(String identifier, Properties properties) {
        ClusterInfo clusterInfo = getClusterInfo(identifier, properties);

        for (int groupId = 0; groupId < clusterInfo.getGroupCount(); groupId++) {
            startAllServersInGroupInternal(identifier, properties, groupId, false);
        }

        // Wait for the Services to come up...
        visitAllServices(identifier, new ServiceStartupWaitVisitor());
    }

    /**
     * Ensure all the servers in the specified group within the specified
     * cluster properties file are started.
     *
     * @param identifier - an identifier for the cluster
     * @param properties - the properties for the cluster
     * @param groupId - the group of servers to start
     */
    public void ensureAllServersInClusterGroup(String identifier, Properties properties, int groupId) {
        startAllServersInGroupInternal(identifier, properties, groupId, true);
    }

    private void startAllServersInGroupInternal(String identifier, Properties properties, int groupId, boolean waitForStart) {
        ClusterInfo clusterInfo = getClusterInfo(identifier, properties);
        if (groupId < clusterInfo.getGroupCount()) {
            for (int instance=0; instance<clusterInfo.getServerCount(groupId); instance++) {
                startServiceInstanceInternal(identifier, properties, groupId, instance, false);
            }
        }

        if (waitForStart) {
            // Wait for the Services to come up...
            visitAllServices(identifier, new ServiceStartupWaitVisitor());
        }
    }

    /**
     * Ensure all the specified server instance within the specified group within the specified
     * cluster properties file is started.
     *
     * @param identifier - an identifier for the cluster
     * @param properties - the properties for the cluster
     * @param groupId - the group of the servers to start belongs to
     * @param instanceId - the instance of the server to start
     */
    public void ensureServerInstance(String identifier, Properties properties, int groupId, int instanceId) {
        startServiceInstanceInternal(identifier, properties, groupId, instanceId, true);
    }

    @SuppressWarnings({"unchecked"})
    private void startServiceInstanceInternal(String identifier, Properties properties, int groupId, int instanceId, boolean waitForStart) {
        LongArray serviceList = getServiceList(identifier, groupId);
        if (!serviceList.exists(instanceId)) {
            ClusterInfo clusterInfo = getClusterInfo(identifier, properties);
            Class<? extends ClassloaderLifecycle> serverClass = clusterInfo.getServerClass(groupId);
            Properties localProperties = clusterInfo.getLocalProperties(groupId);
            localProperties.putAll(extraProperties);
            localProperties.putAll(getPropertyOverrides(identifier, groupId));
            
            ClassloaderRunner runner;
            try {
                runner = new ClassloaderRunner(serverClass.getCanonicalName(), localProperties);
            } catch (Throwable throwable) {
                throw ensureRuntimeException(throwable, "Error starting server clusterFile=" + identifier +
                        " groupId=" + groupId + " instance=" + instanceId);
            }

            serviceList.set(instanceId, runner);
        }

        if (waitForStart) {
            // Wait for the Services to come up...
            visitAllServices(identifier, new ServiceStartupWaitVisitor());
        }
    }

    /**
     * Shutdown all the servers in the specified cluster properties file.
     * @param clusterFilename - the cluster properties file to use to identify the servers to shutdown
     */
    public void shutdown(String clusterFilename) {
        CacheFactory.log("Shutting down all services : " + clusterFilename, CacheFactory.LOG_INFO);
        visitAllServices(clusterFilename, new ServiceShutdownVisitor());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignored
        }
        CacheFactory.log("Shut down all services : " + clusterFilename, CacheFactory.LOG_INFO);
    }

    /**
     * Shutdown all the servers in the specified group within the specified cluster properties file.
     * @param clusterFilename - the cluster properties file to use to identify the servers to shutdown
     * @param groupId - the server group to shut down
     */
    public void shutdown(String clusterFilename, int groupId) {
        CacheFactory.log("Shutting down all services : cluster=" + clusterFilename + " groupId=" + groupId, CacheFactory.LOG_INFO);
        visitAllServicesInGroup(clusterFilename, groupId, new ServiceShutdownVisitor());
        CacheFactory.log("Shut down all services : cluster=" + clusterFilename + " groupId=" + groupId, CacheFactory.LOG_INFO);
    }

    /**
     * Shutdown the specified server instance in the specified group within the specified cluster properties file.
     * @param clusterFilename - the cluster properties file to use to identify the server to shutdown
     * @param groupId - the server group containing the server to shutdown
     * @param instance - the server instance to shutdown
     */
    public void shutdown(String clusterFilename, int groupId, int instance) {
        shutdown(clusterFilename, groupId, instance, new ServiceShutdownVisitor());
    }

    /**
     * Shutdown the specified server instance in the specified group within the specified cluster properties file
     * and returns immediately without waiting to confirm shutdown.
     * This will cleanly shut down nodes so data loss should not occurr.
     * @param clusterFilename - the cluster properties file to use to identify the server to shutdown
     * @param groupId - the server group containing the server to shut down
     * @param instance - the server instance to shut down
     */
    public void shutdownNoWait(String clusterFilename, int groupId, int instance) {
        shutdown(clusterFilename, groupId, instance, new ServiceShutdownAndWaitVisitor());
    }

    private void shutdown(String clusterFilename, int groupId, int instance, ServiceVisitor visitor) {
        CacheFactory.log("Shutting down service : cluster=" + clusterFilename + " groupId=" + groupId + " instance=" + instance, CacheFactory.LOG_INFO);
        visitService(clusterFilename, groupId, instance, visitor);
        CacheFactory.log("Shut down service : cluster=" + clusterFilename + " groupId=" + groupId + " instance=" + instance);
    }

    /**
     * Kill the all server instances in the specified group within the specified cluster properties file.
     * This method will stop a nodes network sockets before stopping the node so simulating node death.
     * @param clusterFilename - the cluster properties file to use to identify the server to kill
     * @param groupId - the server group containing the server to kill
     */
    public void kill(String clusterFilename, int groupId) {
        CacheFactory.log("Killing down all services : cluster=" + clusterFilename + " groupId=" + groupId, CacheFactory.LOG_INFO);
        visitAllServicesInGroup(clusterFilename, groupId, new ServiceKillVisitor());
        CacheFactory.log("Killed all services : cluster=" + clusterFilename + " groupId=" + groupId, CacheFactory.LOG_INFO);
    }

    /**
     * Kill the specified server instance in the specified group within the specified cluster properties file.
     * This method will stop a nodes network sockets before stopping the node so simulating node death.
     * @param clusterFilename - the cluster properties file to use to identify the server to kill
     * @param groupId - the server group containing the server to kill
     * @param instance - the server instance to kill
     */
    public void kill(String clusterFilename, int groupId, int instance) {
        CacheFactory.log("Killing service : cluster=" + clusterFilename + " groupId=" + groupId + " instance=" + instance, CacheFactory.LOG_INFO);
        visitService(clusterFilename, groupId, instance, new ServiceKillVisitor());
        CacheFactory.log("Killed service : cluster=" + clusterFilename + " groupId=" + groupId + " instance=" + instance, CacheFactory.LOG_INFO);
    }

    /**
     * Suspend the network om the specified server instance in the specified group within the specified cluster properties file.
     * @param clusterFilename - the cluster properties file to use to identify the server
     * @param groupId - the server group containing the server
     * @param instance - the server instance
     */
    public void suspendNetwork(String clusterFilename, int groupId, int instance) {
        CacheFactory.log("Suspending Network service : cluster=" + clusterFilename + " groupId=" + groupId + " instance=" + instance, CacheFactory.LOG_INFO);
        visitService(clusterFilename, groupId, instance, new SuspendNetworkVisitor());
        CacheFactory.log("Suspended service : cluster=" + clusterFilename + " groupId=" + groupId + " instance=" + instance, CacheFactory.LOG_INFO);
    }

    /**
     * Unsuspend the network om the specified server instance in the specified group within the specified cluster properties file.
     * @param clusterFilename - the cluster properties file to use to identify the server
     * @param groupId - the server group containing the server
     * @param instance - the server instance
     */
    public void unsuspendNetwork(String clusterFilename, int groupId, int instance) {
        CacheFactory.log("unuspending Network service : cluster=" + clusterFilename + " groupId=" + groupId + " instance=" + instance, CacheFactory.LOG_INFO);
        visitService(clusterFilename, groupId, instance, new UnsuspendNetworkVisitor());
        CacheFactory.log("unuspended service : cluster=" + clusterFilename + " groupId=" + groupId + " instance=" + instance, CacheFactory.LOG_INFO);
    }

    public <T> T invoke(String clusterFilename, int groupId, int instance, String className, String methodName) {
//        return invoke(clusterFilename, groupId, instance, className, methodName, new Class[0], new Object[0]);
        InvokeVisitor<T> visitor = new InvokeVisitor<T>(className, methodName, new Class[0], new Object[0]);
        invoke(clusterFilename, groupId, instance, visitor);
        return visitor.getResult();
    }

    public <T> T invoke(String clusterFilename, int groupId, int instance, String className, String methodName, Class[] paramTypes, Object[] params) {
        InvokeVisitor<T> visitor = new InvokeVisitor<T>(className, methodName, paramTypes, params);
        invoke(clusterFilename, groupId, instance, visitor);
        return visitor.getResult();
    }

    private void invoke(String clusterFilename, int groupId, int instance, InvokeVisitor visitor) {
        visitService(clusterFilename, groupId, instance, visitor);
    }

    ClusterInfo getClusterInfo(String identifier, Properties properties) {
        if (!clusters.containsKey(identifier)) {
            clusters.put(identifier, new ClusterInfo(properties));
        }
        return clusters.get(identifier);
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
        public boolean visit(ClassloaderRunner service) {
            try {
                while(!service.isStarted()) {
                    CacheFactory.log("Waiting for " + service, CacheFactory.LOG_DEBUG);
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
        public boolean visit(ClassloaderRunner service) {
            try {
                CacheFactory.log("Shutting down " + service, CacheFactory.LOG_DEBUG);
                service.shutdown();
                return true;
            } catch (Exception e) {
                throw Base.ensureRuntimeException(e, "Error shutting down service " + service);
            }
        }
    }

    /**
     * ServiceVisitor that shuts down services and waits for the service to stop
     */
    private static class ServiceShutdownAndWaitVisitor extends ServiceShutdownVisitor {
        /**
         * Shutdown the specified ClassloaderRunner
         * @param service the ClassloaderRunner to visit
         */
        @Override
        public boolean visit(ClassloaderRunner service) {
            try {
                super.visit(service);
                while(service.isStarted()) {
                    Thread.sleep(100);
                }
                return true;
            } catch (Exception e) {
                throw Base.ensureRuntimeException(e, "Error shutting down service " + service);
            }
        }
    }

    /**
     * ServiceVisitor that Kills services by stopping their
     * network sockets before stopping the service
     */
    private static class ServiceKillVisitor implements ServiceVisitor {
        /**
         * Shutdown the specified ClassloaderRunner
         * @param service the ClassloaderRunner to visit
         */
        public boolean visit(ClassloaderRunner service) {
            try {
                CacheFactory.log("Shutting down " + service, CacheFactory.LOG_DEBUG);
                service.suspendNetwork();
                service.shutdown();
                return true;
            } catch (Exception e) {
                throw Base.ensureRuntimeException(e, "Error shutting down service " + service);
            }
        }
    }

    private static class InvokeVisitor<T> implements ServiceVisitor {
        private String className;
        private String methodName;
        private Class[] paramTypes;
        private Object[] params;
        private T result;

        private InvokeVisitor(String className, String methodName, Class[] paramTypes, Object[] params) {
            this.className = className;
            this.methodName = methodName;
            this.paramTypes = paramTypes;
            this.params = params;
        }

        public T getResult() {
            return result;
        }

        @SuppressWarnings({"unchecked"})
        public boolean visit(ClassloaderRunner service) {
            try {
                result = (T) service.invoke(className, methodName, paramTypes, params);
                return false;
            } catch (Exception e) {
                throw Base.ensureRuntimeException(e, "Error invoking method on service " + service);
            }
        }
    }

    /**
     * ServiceVisitor that suspends sending and receiving of packets
     * by network sockets for a service
     */
    private static class SuspendNetworkVisitor implements ServiceVisitor {
        /**
         * @param service the ClassloaderRunner to visit
         */
        public boolean visit(ClassloaderRunner service) {
            try {
                CacheFactory.log("Suspending Network " + service, CacheFactory.LOG_DEBUG);
                service.suspendNetwork();
                return false;
            } catch (Exception e) {
                throw Base.ensureRuntimeException(e, "Error shutting down service " + service);
            }
        }
    }

    /**
     * ServiceVisitor that re-enables sending and receiving of packets
     * by network sockets for a service
     */
    private static class UnsuspendNetworkVisitor implements ServiceVisitor {
        /**
         * @param service the ClassloaderRunner to visit
         */
        public boolean visit(ClassloaderRunner service) {
            try {
                CacheFactory.log("Unsuspending Network " + service);
                service.unsuspendNetwork();
                return false;
            } catch (Exception e) {
                throw Base.ensureRuntimeException(e, "Error shutting down service " + service);
            }
        }
    }
}
