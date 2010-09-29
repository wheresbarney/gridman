package org.gridman.coherence.security.kerberos;

import com.tangosol.net.security.IdentityTransformer;
import org.gridman.kerberos.KrbHelper;

import javax.security.auth.Subject;

/**
 * @author Jonathan Knight
 */
public class KrbIdentityTransformer implements IdentityTransformer {

    private String spn;

    public KrbIdentityTransformer(String spn) {
        this.spn = spn;
    }

    @Override
    public Object transformIdentity(Subject subject) throws SecurityException {
        return KrbHelper.getServiceTicket(subject, spn);
    }

}
