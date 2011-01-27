package org.gridman.testtools.coherence.classloader;

import com.tangosol.net.CacheFactory;
import com.tangosol.util.Base;
import com.tangosol.util.LongArray;
import com.tangosol.util.SimpleLongArray;
import org.gridman.testtools.classloader.ClassloaderLifecycle;
import org.gridman.testtools.classloader.ClassloaderRunner;
import org.gridman.testtools.classloader.SystemPropertyLoader;
import org.gridman.testtools.coherence.queries.ClusterQuery;

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
    private Map<String, Map<Integer, Properties>> servicePropertyOverrides;

    private Properties extraProperties;

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            Properties properties = SystemPropertyLoader.getSystemProperties(args[0]);
            ClusterInfo clusterInfo = new ClusterInfo(args[0], properties);
            ClusterStarter clusterStarter = new ClusterStarter();
            if (args.length == 1) {
                clusterStarter.ensureCluster(clusterInfo);
            } else if (args.length == 2) {
                int groupId = Integer.parseInt(args[1]);
                ClusterNodeGroup group = new ClusterNodeGroup(clusterInfo, groupId);
                clusterStarter.startAllServersInGroupInternal(group, false);
            } else if (args.length == 3) {
                int groupId = Integer.parseInt(args[1]);
                int instance = Integer.parseInt(args[2]);
                ClusterNodeGroup group = new ClusterNodeGroup(clusterInfo, groupId);
                ClusterNode node = new ClusterNode(group, instance);
                clusterStarter.startServiceInstanceInternal(node, false);
            }
        }

        final Object lock = new Object();
        synchronized (lock) {
            lock.wait();
        }
    }

    private ClusterStarter() {
        clusters = new HashMap<String, ClusterInfo>();
        services = new HashMap<String, LongArray>();
        servicePropertyOverrides = new HashMap<String, Map<Integer, Properties>>();
        extraProperties = new Properties();
    }

    public ClusterStarter setProperty(String key, String value) {
        extraProperties.setProperty(key, value);
        return this;
    }

    public ClusterStarter overrideClusterProperty(ClusterNodeGroup group, String key, String value) {
        getPropertyOverrides(group).setProperty(key, value);
        return this;
    }

    private Properties getPropertyOverrides(ClusterNodeGroup group) {
        String identifier = group.getClusterInfo().getIdentifier();
        if (!servicePropertyOverrides.containsKey(identifier)) {
            servicePropertyOverrides.put(identifier, new HashMap<Integer, Properties>());
        }
        Map<Integer, Properties> groupOverrides = servicePropertyOverrides.get(identifier);
        if (!groupOverrides.containsKey(group.getGroupId())) {
            groupOverrides.put(group.getGroupId(), new Properties());
        }
        return groupOverrides.get(group.getGroupId());
    }

    /**
     * Returns the singleton instance of ClusterStarter
     *
     * @return the singleton instance of ClusterStarter
     */
    public static synchronized ClusterStarter getInstance() {
        if (sInstance == null) {
            sInstance = new ClusterStarter();
        }
        return sInstance;
    }

    /**
     * Ensure all the servers in all the server groups of the specified
     * cluster file are started.
     *
     * @param clusterInfo - the properties file for the cluster to ensure
     */
    public void ensureCluster(ClusterInfo clusterInfo) {
        for (Object group : clusterInfo.getGroups()) {
            startAllServersInGroupInternal((ClusterNodeGroup)group, false);
        }

        // Wait for the Services to come up...
        visitAllServices(clusterInfo, new ServiceStartupWaitVisitor());
    }

    /**
     * Ensure all the servers in the specified group within the specified
     * cluster properties file are started.
     *
     * @param group    - the group of servers to start
     */
    public void ensureAllServersInClusterGroup(ClusterNodeGroup group) {
        startAllServersInGroupInternal(group, true);
    }

    private void startAllServersInGroupInternal(ClusterNodeGroup group, boolean waitForStart) {
        ClusterInfo clusterInfo = group.getClusterInfo();
        int groupId = group.getGroupId();
        if (groupId < clusterInfo.getGroupCount()) {
            for (Object node : clusterInfo.getNodesForGroup(groupId)) {
                startServiceInstanceInternal((ClusterNode)node, false);
            }
        }

        if (waitForStart) {
            // Wait for the Services to come up...
            visitAllServices(clusterInfo, new ServiceStartupWaitVisitor());
        }
    }

    /**
     * Ensure all the specified server instance within the specified group within the specified
     * cluster properties file is started.
     *
     * @param clusterNode - the instance of the server to start
     */
    public void ensureServerInstance(ClusterNode clusterNode) {
        startServiceInstanceInternal(clusterNode, true);
    }

    @SuppressWarnings({"unchecked"})
    private void startServiceInstanceInternal(ClusterNode node, boolean waitForStart) {
        LongArray serviceList = getServiceList(node.getGroup());
        ClusterInfo clusterInfo = node.getClusterInfo();
        int groupId = node.getGroupId();
        int instanceId = node.getNodeId();

        if (!serviceList.exists(node.getNodeId())) {
            Class<? extends ClassloaderLifecycle> serverClass = clusterInfo.getServerClass(groupId);
            Properties localProperties = clusterInfo.getLocalProperties(groupId);
            localProperties.putAll(extraProperties);
            localProperties.putAll(getPropertyOverrides(node.getGroup()));

            ClassloaderRunner runner;
            try {
                runner = new ClassloaderRunner(serverClass.getCanonicalName(), localProperties);
            } catch (Throwable throwable) {
                throw ensureRuntimeException(throwable, "Error starting server clusterFile=" + clusterInfo.getIdentifier() +
                        " groupId=" + groupId + " instance=" + instanceId);
            }

            serviceList.set(instanceId, runner);
        }

        if (waitForStart) {
            // Wait for the Services to come up...
            visitAllServices(clusterInfo, new ServiceStartupWaitVisitor());
        }
    }

    /**
     * Shutdown all the servers in the specified cluster properties file.
     *
     * @param clusterInfo - the cluster properties file to use to identify the servers to shutdown
     */
    public void shutdown(ClusterInfo clusterInfo) {
        CacheFactory.log("Shutting down all services : " + clusterInfo, CacheFactory.LOG_INFO);
        visitAllServices(clusterInfo, new ServiceShutdownVisitor());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignored
        }
        CacheFactory.log("Shut down all services : " + clusterInfo, CacheFactory.LOG_INFO);
    }

    /**
     * Shutdown all the servers in the specified group within the specified cluster properties file.
     *
     * @param group     - the server group to shut down
     */
    public void shutdown(ClusterNodeGroup group) {
        CacheFactory.log("Shutting down all services : " + group, CacheFactory.LOG_INFO);
        visitAllServicesInGroup(group, new ServiceShutdownVisitor());
        CacheFactory.log("Shut down all services : " + group, CacheFactory.LOG_INFO);
    }

    /**
     * Shutdown the specified server instance in the specified group within the specified cluster properties file.
     *
     * @param node        - the server instance to shutdown
     */
    public void shutdown(ClusterNode node) {
        shutdown(node, new ServiceShutdownVisitor());
    }

    /**
     * Shutdown the specified server instance in the specified group within the specified cluster properties file
     * and returns immediately without waiting to confirm shutdown.
     * This will cleanly shut down nodes so data loss should not occurr.
     *
     * @param node        - the server instance to shut down
     */
    public void shutdownNoWait(ClusterNode node) {
        shutdown(node, new ServiceShutdownAndWaitVisitor());
    }

    private void shutdown(ClusterNode node, ServiceVisitor visitor) {
        CacheFactory.log("Shutting down service : " + node, CacheFactory.LOG_INFO);
        visitService(node, visitor);
        CacheFactory.log("Shut down service : " + node, CacheFactory.LOG_INFO);
    }

    /**
     * Kill the all server instances in the specified group within the specified cluster properties file.
     * This method will stop a nodes network sockets before stopping the node so simulating node death.
     *
     * @param group - the server group containing the server to killNode
     */
    public void killNode(ClusterNodeGroup group) {
        CacheFactory.log("Killing down all services : " + group, CacheFactory.LOG_INFO);
        visitAllServicesInGroup(group, new ServiceKillVisitor());
        CacheFactory.log("Killed all services : " + group, CacheFactory.LOG_INFO);
    }

    /**
     * Kill the specified server instance in the specified group within the specified cluster properties file.
     * This method will stop a nodes network sockets before stopping the node so simulating node death.
     *
     * @param instance        - the server instance to kill
     */
    public void killNode(ClusterNode instance) {
        CacheFactory.log("Killing service : " + instance, CacheFactory.LOG_INFO);
        visitService(instance, new ServiceKillVisitor());
        CacheFactory.log("Killed service : " + instance, CacheFactory.LOG_INFO);
    }

    /**
     * Suspend the network om the specified server instance in the specified group within the specified cluster properties file.
     *
     * @param instance        - the server instance
     */
    public void suspendNetwork(ClusterNode instance) {
        CacheFactory.log("Suspending Network service : " + instance, CacheFactory.LOG_INFO);
        visitService(instance, new SuspendNetworkVisitor());
        CacheFactory.log("Suspended service : " + instance, CacheFactory.LOG_INFO);
    }

    /**
     * Unsuspend the network om the specified server instance in the specified group within the specified cluster properties file.
     *
     * @param instance    - the server instance
     */
    public void unsuspendNetwork(ClusterNode instance) {
        CacheFactory.log("unuspending Network service : " + instance, CacheFactory.LOG_INFO);
        visitService(instance, new UnsuspendNetworkVisitor());
        CacheFactory.log("unuspended service : " + instance, CacheFactory.LOG_INFO);
    }

    public <T> T invoke(ClusterNode node, ClusterQuery<T> query) {
        InvokeVisitor<T> visitor = new InvokeVisitor<T>(query);
        invoke(node, visitor);
        return visitor.getResult();
    }

    public <T> T invoke(ClusterNode node, String className, String methodName) {
        InvokeVisitor<T> visitor = new InvokeVisitor<T>(className, methodName, new Class[0], new Object[0]);
        invoke(node, visitor);
        return visitor.getResult();
    }

    public <T> T invoke(ClusterNode node, String className, String methodName, Class[] paramTypes, Object[] params) {
        InvokeVisitor<T> visitor = new InvokeVisitor<T>(className, methodName, paramTypes, params);
        invoke(node, visitor);
        return visitor.getResult();
    }

    private void invoke(ClusterNode node, InvokeVisitor visitor) {
        visitService(node, visitor);
    }

    LongArray getClusterServiceList(ClusterInfo clusterInfo) {
        String identifier = clusterInfo.getIdentifier();
        if (!services.containsKey(identifier)) {
            services.put(identifier, new SimpleLongArray());
        }
        return services.get(identifier);
    }

    LongArray getServiceList(ClusterNodeGroup group) {
        LongArray clusterServiceList = getClusterServiceList(group.getClusterInfo());
        if (!clusterServiceList.exists(group.getGroupId())) {
            clusterServiceList.set(group.getGroupId(), new SimpleLongArray());
        }
        return (LongArray) clusterServiceList.get(group.getGroupId());
    }

    @SuppressWarnings({"unchecked"})
    void visitAllServices(ClusterInfo clusterInfo, ServiceVisitor visitor) {
        LongArray clusterServicesList = getClusterServiceList(clusterInfo);
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
    void visitAllServicesInGroup(ClusterNodeGroup group, ServiceVisitor visitor) {
        LongArray clusterServicesList = getClusterServiceList(group.getClusterInfo());
        LongArray serviceList = (LongArray) clusterServicesList.get(group.getGroupId());
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
    void visitService(ClusterNode node, ServiceVisitor visitor) {
        LongArray clusterServicesList = getClusterServiceList(node.getClusterInfo());
        LongArray serviceList = (LongArray) clusterServicesList.get(node.getGroupId());
        if (serviceList != null) {
            ClassloaderRunner service = (ClassloaderRunner) serviceList.get(node.getNodeId());
            if (service != null) {
                if (visitor.visit(service)) {
                    serviceList.remove(node.getNodeId());
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
         *
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
         *
         * @param service the ClassloaderRunner to visit
         */
        public boolean visit(ClassloaderRunner service) {
            try {
                while (!service.isStarted()) {
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
         *
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
         *
         * @param service the ClassloaderRunner to visit
         */
        @Override
        public boolean visit(ClassloaderRunner service) {
            try {
                super.visit(service);
                while (service.isStarted()) {
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
         *
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

        private InvokeVisitor(ClusterQuery query) {
            this.className = query.getClassName();
            this.methodName = query.getMethodName();
            this.paramTypes = query.getParamTypes();
            this.params = query.getParameters();
        }

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
