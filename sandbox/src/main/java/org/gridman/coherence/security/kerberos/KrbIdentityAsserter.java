package org.gridman.coherence.security.kerberos;

import com.tangosol.net.security.IdentityAsserter;
import org.gridman.kerberos.KrbHelper;
import org.gridman.kerberos.KrbTicket;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jonathan Knight
 */
public class KrbIdentityAsserter implements IdentityAsserter {

    @Override
    public Subject assertIdentity(Object token) throws SecurityException {
        Subject subject;

        Set<Principal> principals = new HashSet<Principal>();

        KrbTicket principal = KrbHelper.validate((byte[])token);
        principals.add(principal);
        
        subject = new Subject(true, principals, new HashSet(), new HashSet());
        return subject;
    }

}
