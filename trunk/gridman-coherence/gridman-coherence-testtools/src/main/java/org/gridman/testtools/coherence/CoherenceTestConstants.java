package org.gridman.testtools.coherence;

/**
 * A set of constants for common values when using the
 * /coherence/common/common-cluster.properties file to
 * create a test cluster.
 * 
 * @author Jonathan Knight
 */
public interface CoherenceTestConstants {

    String COMMON_CLUSTER_FILE = "/coherence/common/common-cluster.properties";
    int COMMON_STORAGE_GROUP = 0;
    int COMMON_EXTENDPROXY_GROUP = 1;

    String COMMON_CLIENT_PROPERTIES = "/coherence/common/common-client.properties";
}
