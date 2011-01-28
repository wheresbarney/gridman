package org.gridman.testtools.coherence.classloader;

import com.tangosol.net.CacheFactory;
import com.tangosol.util.Base;
import org.gridman.testtools.classloader.ClassloaderLifecycle;
import org.gridman.testtools.classloader.ClassloaderRunner;
import org.gridman.testtools.classloader.SystemPropertyLoader;
import org.gridman.testtools.coherence.queries.ClusterQuery;

import java.util.*;

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

    private Map<ClusterInfo,Map<ClusterNodeGroup,Map<ClusterNode,ClassloaderRunner>>> services;

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
        services = new HashMap<ClusterInfo,Map<ClusterNodeGroup,Map<ClusterNode,ClassloaderRunner>>>();
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
        ClusterInfo clusterInfo = node.getClusterInfo();
        int groupId = node.getGroupId();
        int instanceId = node.getNodeId();

        Map<ClusterNode, ClassloaderRunner> servicesForGroup = servicesForGroup(node.getGroup());

        if (!servicesForGroup.containsKey(node)) {
            try {
                Class<? extends ClassloaderLifecycle> serverClass = clusterInfo.getServerClass(node.getGroup());
                Properties localProperties = clusterInfo.getGroupProperties(groupId);
                localProperties.putAll(node.getGroup().getOverrideProperties());
                localProperties.putAll(node.getOverrideProperties());

                ClassloaderRunner runner = new ClassloaderRunner(serverClass.getCanonicalName(), localProperties);
                servicesForGroup.put(node, runner);
            } catch (Throwable throwable) {
                throw ensureRuntimeException(throwable, "Error starting server clusterFile=" + clusterInfo.getIdentifier() +
                        " groupId=" + groupId + " instance=" + instanceId);
            }
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
        visitAllServices(group, new ServiceShutdownVisitor());
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
        visitAllServices(group, new ServiceKillVisitor());
        CacheFactory.log("Killed all services : " + group, CacheFactory.LOG_INFO);
    }

    /**
     * Kill the specified server instance in the specified group within the specified cluster properties file.
     * This method will stop a nodes network sockets before stopping the node so simulating node death.
     *
     * @param nodes        - the server instances to kill
     */
    public void killNode(ClusterNode... nodes) {
        CacheFactory.log("Killing service : instances=" + Arrays.toString(nodes), CacheFactory.LOG_INFO);
        for (ClusterNode node : nodes) {
            visitService(node, new SuspendNetworkVisitor());
        }
        for (ClusterNode node : nodes) {
            visitService(node, new ServiceShutdownVisitor());
        }
        CacheFactory.log("Killed service : instances=" + Arrays.toString(nodes), CacheFactory.LOG_INFO);
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

    private Map<ClusterNodeGroup,Map<ClusterNode,ClassloaderRunner>> servicesForCluster(ClusterInfo clusterInfo) {
        if (!services.containsKey(clusterInfo)) {
            services.put(clusterInfo, new TreeMap<ClusterNodeGroup,Map<ClusterNode,ClassloaderRunner>>());
        }
        return services.get(clusterInfo);
    }

    private Map<ClusterNode,ClassloaderRunner> servicesForGroup(ClusterNodeGroup group) {
        Map<ClusterNodeGroup,Map<ClusterNode,ClassloaderRunner>> servicesForCluster = servicesForCluster(group.getClusterInfo());
        if (!servicesForCluster.containsKey(group)) {
            servicesForCluster.put(group, new TreeMap<ClusterNode,ClassloaderRunner>());
        }
        return servicesForCluster.get(group);
    }

    private void visitAllServices(ClusterInfo clusterInfo, ServiceVisitor visitor) {
        Map<ClusterNodeGroup,Map<ClusterNode,ClassloaderRunner>> clusterServices = services.get(clusterInfo);
        if (clusterServices != null) {
            TreeSet<ClusterNodeGroup> groups = new TreeSet<ClusterNodeGroup>(clusterServices.keySet());
            for(ClusterNodeGroup group : groups) {
                visitAllServices(clusterServices.get(group), visitor);
            }
        }
    }

    private void visitAllServices(ClusterNodeGroup group, ServiceVisitor visitor) {
        Map<ClusterNodeGroup,Map<ClusterNode,ClassloaderRunner>> clusterServices = services.get(group.getClusterInfo());
        if (clusterServices != null) {
            Map<ClusterNode,ClassloaderRunner> nodeServices = clusterServices.get(group);
            visitAllServices(nodeServices, visitor);
        }
    }

    private void visitAllServices(Map<ClusterNode,ClassloaderRunner> nodeServices, ServiceVisitor visitor) {
        TreeSet<ClusterNode> nodes = new TreeSet<ClusterNode>(nodeServices.keySet());
        for (ClusterNode node : nodes) {
            visitService(node, visitor);
        }
    }

    private void visitService(ClusterNode node, ServiceVisitor visitor) {
        ClusterNodeGroup group = node.getGroup();
        Map<ClusterNodeGroup,Map<ClusterNode,ClassloaderRunner>> clusterServices = services.get(group.getClusterInfo());
        if (clusterServices != null) {
            Map<ClusterNode,ClassloaderRunner> nodeServices = clusterServices.get(group);
            if (nodeServices != null) {
                ClassloaderRunner service = nodeServices.get(node);
                if (service != null) {
                    if (visitor.visit(service)) {
                        nodeServices.remove(node);
                    }
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
