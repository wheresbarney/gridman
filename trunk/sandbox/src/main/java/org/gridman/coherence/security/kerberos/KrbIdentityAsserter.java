package org.gridman.coherence.security.kerberos;

import com.tangosol.net.security.IdentityAsserter;
import org.gridman.security.JaasHelper;
import org.gridman.security.kerberos.KrbHelper;
import org.gridman.security.kerberos.KrbTicket;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link com.tangosol.net.security.IdentityAsserter} that will
 * assert that the Object passed to the assertIdentity method is a valid Kerberos
 * ticket in the form of a byte toByteArray.
 * </p>
 * This IdentityAsserter is configurable to indicate whether it should ignore
 * Kerberos replay errors, that is instances where identical tickets are sent
 * for validation.
 *
 * @author Jonathan Knight
 */
public class KrbIdentityAsserter implements IdentityAsserter {

    /** Flag to indicate whether this asserter ignores Kerberos replay errors */
    private boolean allowReplays = false;

    /** The Subject that will be used to authenticate the token being asserted */
    private Subject authenticatingSubject;

    /**
     * Default constructor that will create a KrbIdentityAsserter
     * that will not allow replay errors
     */
    public KrbIdentityAsserter() {
        this.authenticatingSubject = JaasHelper.ensureCurrentSubject();
    }

    /**
     * Constructor that will create a KrbIdentityAsserter that
     * will set the allowReplays flag to the specified value.
     * </p>
     * If allowReplays is true then replay errors will be ignored
     * in the assertIdentity method otherwise if allowReplays is false
     * then replay errors will cause a SecurityException to be thrown.
     *
     * @param allowReplays - flag to indicate whether to allow replay errors
     */
    public KrbIdentityAsserter(boolean allowReplays) {
        this.allowReplays = allowReplays;
    }

    /**
     * Assert that the specified token is a byte[] and is a valid
     * Kerberos ticket.
     * </p>
     *
     * @param token - a byte[] Kerberos ticket 
     * @return a {@link javax.security.auth.Subject}
     * @throws SecurityException
     */
    @Override
    public Subject assertIdentity(Object token) throws SecurityException {
        Subject subject;

        if (!(token instanceof byte[])) {
            throw new SecurityException("Invalid Token - expected valid Kerberos ticket in the form of a byte[]");
        }
        
        Set<Principal> principals = new HashSet<Principal>();

        KrbTicket principal = KrbHelper.validate(authenticatingSubject, (byte[])token, allowReplays);
        principals.add(principal);
        
        subject = new Subject(true, principals, new HashSet(), new HashSet());
        return subject;
    }

}
