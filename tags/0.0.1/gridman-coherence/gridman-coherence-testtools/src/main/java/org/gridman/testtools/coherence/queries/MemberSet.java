package org.gridman.testtools.coherence.queries;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Member;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jonathan Knight
 */
public class MemberSet extends BaseQuery<Set<String>> {

    @SuppressWarnings({"unchecked"})
    @Override
    public Set<String> run() {
        Set<String> members = new HashSet<String>();

        Cluster cluster = CacheFactory.getCluster();
        if (cluster != null) {
            for (Member member : (Set<Member>)cluster.getMemberSet()) {
                members.add(member.toString());
            }
        }

        return members;
    }
}
