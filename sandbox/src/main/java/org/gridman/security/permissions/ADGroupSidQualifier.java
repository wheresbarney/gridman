package org.gridman.security.permissions;

import org.gridman.coherence.security.simple.CoherenceSecurityUtils;
import org.gridman.security.kerberos.KrbTicket;
import org.gridman.security.kerberos.activedirectory.Pac;
import org.gridman.security.kerberos.activedirectory.PacBufferType;
import org.gridman.security.kerberos.activedirectory.PacLogonInfo;
import org.gridman.security.kerberos.activedirectory.PacSid;

import javax.security.auth.Subject;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jonathan Knight
 */
public class ADGroupSidQualifier implements PermissionQualifier {

    private Set<PacSid> qualifyingGroups;

    /**
     * Create a new ADGroupSidQualifier using the specified array
     * of Active Directory Group SID String values as qualifying groups.
     *
     * @param groupSIDs - array of Active Directory Group SID String values
     */
    public ADGroupSidQualifier(String... groupSIDs) {
        if (groupSIDs == null || groupSIDs.length == 0) {
            throw new IllegalArgumentException("The groupSIDs parameter cannot be null or empty");
        }

        qualifyingGroups = new HashSet<PacSid>();
        for (String sid : groupSIDs) {
            qualifyingGroups.add(PacSid.parse(sid));
        }
    }

    /**
     * To qualify the Subject must contain a valid AD PAC
     * which in turn must contain one of the qualifying AD groups.
     * <p/>
     * @param subject - the subject to be verified
     * @return true of the Subject contains an Active Directory PAC containing one of the qualifying groups.
     */
    @Override
    public boolean qualifies(Subject subject) {
        boolean qualifies = false;

        KrbTicket ticket = CoherenceSecurityUtils.getFirstPrincipal(subject, KrbTicket.class);
        if (ticket != null) {
            Pac pac = ticket.getPAC();
            if (pac != null) {
                PacLogonInfo logonInfo = pac.getBuffer(PacBufferType.PAC_LOGON_INFO);
                if (logonInfo != null) {
                    Set<PacSid> groups = logonInfo.getGroupSids();
                    if (!groups.isEmpty()) {
                        for (PacSid qualifyingGroup : qualifyingGroups) {
                            if (groups.contains(qualifyingGroup)) {
                                qualifies = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return qualifies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ADGroupSidQualifier that = (ADGroupSidQualifier) o;

        return qualifyingGroups.equals(that.qualifyingGroups);
    }

    @Override
    public int hashCode() {
        return qualifyingGroups.hashCode();
    }
}
