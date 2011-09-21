package org.gridman.coherence.backup;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.*;
import com.tangosol.util.Base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jonathan Knight
 */
public class NodeExitInvocable extends AbstractInvocable implements PortableObject, InvocationObserver {
    private String role;
    private int numberToKill;

    public NodeExitInvocable() {
    }

    public NodeExitInvocable(String role, int numberToKill) {
        this.role = role;
        this.numberToKill = numberToKill;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void run() {
        List<Member> membersInRole = new ArrayList<Member>();

        Cluster cluster = CacheFactory.ensureCluster();
        Set<Member> members = cluster.getMemberSet();
        for (Member member : members) {
            if (member.getRoleName().equals(role)) {
                membersInRole.add(member);
            }
        }

        membersInRole = Base.randomize(membersInRole);
        Set<Member> membersToKill = new HashSet<Member>();
        for (int i=0; i<numberToKill; i++) {
            membersToKill.add(membersInRole.get(i));
        }

        InvocationService service = (InvocationService) CacheFactory.getService("ClientInvokeService");
        service.execute(new SystemExitInvocable(), membersToKill, this);
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {
        role = pofReader.readString(100);
        numberToKill = pofReader.readInt(101);
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeString(100, role);
        pofWriter.writeInt(101, numberToKill);
    }

    @Override
    public void memberCompleted(Member member, Object o) {
    }

    @Override
    public void memberFailed(Member member, Throwable throwable) {
    }

    @Override
    public void memberLeft(Member member) {
    }

    @Override
    public void invocationCompleted() {
    }
}
