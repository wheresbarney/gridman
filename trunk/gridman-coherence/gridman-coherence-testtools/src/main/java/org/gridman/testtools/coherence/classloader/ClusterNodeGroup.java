package org.gridman.testtools.coherence.classloader;

import java.util.Properties;
import java.util.Set;

/**
 * @author Jonathan Knight
 */
public class ClusterNodeGroup implements Comparable<ClusterNodeGroup> {

    private ClusterInfo clusterInfo;

    private int groupId;

    private Properties overrides;

    public ClusterNodeGroup(ClusterInfo clusterInfo, int groupId) {
        this.clusterInfo = clusterInfo;
        this.groupId = groupId;
        this.overrides = new Properties();
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public int getGroupId() {
        return groupId;
    }

    public Set<ClusterNode> getNodes() {
        return clusterInfo.getNodesForGroup(groupId);
    }

    public ClusterNodeGroup overrideProperty(String key, String value) {
        overrides.setProperty(key, value);
        return this;
    }

    public ClusterNodeGroup removeOverrideProperty(String key) {
        overrides.remove(key);
        return this;
    }

    public Properties getOverrideProperties() {
        return new Properties(overrides);
    }
    
    @Override
    public String toString() {
        return clusterInfo.toString() + " group=" + groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClusterNodeGroup that = (ClusterNodeGroup) o;

        if (groupId != that.groupId) {
            return false;
        }
        if (clusterInfo != null ? !clusterInfo.equals(that.clusterInfo) : that.clusterInfo != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = clusterInfo != null ? clusterInfo.hashCode() : 0;
        result = 31 * result + groupId;
        return result;
    }

    @Override
    public int compareTo(ClusterNodeGroup other) {
        int result = this.clusterInfo.getIdentifier().compareTo(other.getClusterInfo().getIdentifier());
        if (result == 0) {
            result = this.groupId - other.groupId;
        }
        return result;
    }
}
