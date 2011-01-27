package org.gridman.testtools.coherence.classloader;

/**
 * @author Jonathan Knight
 */
public class ClusterNodeGroup implements Comparable<ClusterNodeGroup> {

    private ClusterInfo clusterInfo;

    private int groupId;

    public ClusterNodeGroup(ClusterInfo clusterInfo, int groupId) {
        this.clusterInfo = clusterInfo;
        this.groupId = groupId;
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public int getGroupId() {
        return groupId;
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
