package org.gridman.testtools.coherence.classloader;

/**
 * @author Jonathan Knight
 */
public class ClusterNode implements Comparable<ClusterNode> {

    private ClusterNodeGroup group;

    private int nodeId;

    public ClusterNode(ClusterNodeGroup group, int nodeId) {
        this.group = group;
        this.nodeId = nodeId;
    }

    public ClusterNode(ClusterInfo clusterInfo, int groupId, int nodeId) {
        this.group = new ClusterNodeGroup(clusterInfo, groupId);
        this.nodeId = nodeId;
    }

    public ClusterNodeGroup getGroup() {
        return group;
    }

    public ClusterInfo getClusterInfo() {
        return group.getClusterInfo();
    }

    public int getGroupId() {
        return group.getGroupId();
    }

    public int getNodeId() {
        return nodeId;
    }

    @Override
    public String toString() {
        return group.toString() + " node=" + nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClusterNode that = (ClusterNode) o;

        if (nodeId != that.nodeId) {
            return false;
        }
        if (group != null ? !group.equals(that.group) : that.group != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + nodeId;
        return result;
    }

    @Override
    public int compareTo(ClusterNode other) {
        int result = this.group.compareTo(other.group);
        if (result == 0) {
            result = this.nodeId - other.nodeId;
        }
        return result;
    }
}
