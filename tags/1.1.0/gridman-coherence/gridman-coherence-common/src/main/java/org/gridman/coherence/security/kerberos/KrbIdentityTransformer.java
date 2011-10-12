package org.gridman.coherence.security.kerberos;

import com.tangosol.net.Service;
import com.tangosol.net.security.IdentityTransformer;
import org.gridman.security.kerberos.KrbHelper;

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
    public Object transformIdentity(Subject subject, Service arg1) throws SecurityException {
        if (subject == null) {
            throw new SecurityException("Missing Credentials - Subject not present");
        }
        return KrbHelper.getServiceTicket(subject, spn);
    }

}
