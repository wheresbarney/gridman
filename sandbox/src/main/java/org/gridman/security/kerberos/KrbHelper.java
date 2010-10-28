package org.gridman.security.kerberos;

import org.ietf.jgss.*;
import sun.security.krb5.internal.KrbApErrException;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;
import java.util.Set;

import static org.gridman.security.JaasHelper.doAs;
import static org.gridman.security.JaasHelper.getCurrentSubject;

/**
 * @author Jonathan Knight
 */
public class KrbHelper {

    /**
     * Obtain a new Kerberos ticket for a given service principal name.
     * <p/>
     * If the {@link javax.security.auth.Subject} parameter is not null then this subject is used
     * to obtain a ticket otherwise the {@link javax.security.auth.Subject} returned by calling
     * {@link KrbHelper} ensureCurrentSubject() will be used.
     *
     * @param subject - the {@link javax.security.auth.Subject} to use to obtain the ticket
     * @param spn - the service principal name to get a ticket for
     * @return a Kerberos ticket for the specified service
     */
    public static byte[] getServiceTicket(Subject subject, final String spn) {
        return doAs(subject, new GetServiceTicketAction(spn));
    }

    /**
     * Validate the specified kerberos ticket.<p/>
     *
     * @param subject - the Subject to use to authenticate the ticket
     * @param ticket - the Kerberos session ticket to be validated
     * @param allowReplay - flag to indicate whether replay errors are ignored
     * @return a {@link KrbTicket} instance wrapping the validated ticket
     */
    public static KrbTicket validate(Subject subject, byte[] ticket, boolean allowReplay) {
        return doAs(subject, new ValidateTicketAction(ticket, allowReplay));
    }

    /**
     * Validate the specified kerberos ticket.<p/>
     *
     * @param ticket - the Kerberos session ticket to be validated
     * @param allowReplay - flag to indicate whether replay errors are ignored
     * @return a {@link KrbTicket} instance wrapping the validated ticket
     */
    public static KrbTicket validate(byte[] ticket, boolean allowReplay) {
        return validate(getCurrentSubject(), ticket, allowReplay);
    }

    /**
     * Validate the specified kerberos ticket using
     * the specified {@link Subject}.<p/>
     *
     * @param ticket - the Kerberos session ticket to be validated
     * @param allowReplay - flag to indicate whether replay errors are ignored
     * @param subject - the {@link Subject} to use to validate the ticket.
     * @return a {@link KrbTicket} instance wrapping the validated ticket
     */
    public static KrbTicket validate(byte[] ticket, boolean allowReplay, Subject subject) {
        return doAs(subject, new ValidateTicketAction(ticket, allowReplay));
    }

    /**
     * Extract the {@link KerberosPrincipal} from the set of {@link Principal}
     * instances contained within the specified {@link Subject}.
     * </p>
     * If the {@link Subject} contains multiple {@link KerberosPrincipal} instances
     * then the exact instance returned is undetermined.
     * If there are no {@link KerberosPrincipal} instances contained within
     * the {@link Subject} this method will return null.
     * </p>
     * @param subject - the {@link Subject} to extract the {@link KerberosPrincipal} from.
     * @return The {@link KerberosPrincipal} contained within the specified {@link Subject}
     *         or null if there are no {@link KerberosPrincipal} instances.
     */
    public static KerberosPrincipal extractKerberosPrincipal(Subject subject) {
        KerberosPrincipal principal = null;

        Set<KerberosPrincipal> principals = subject.getPrincipals(KerberosPrincipal.class);
        if (!principals.isEmpty()) {
            principal = principals.iterator().next();
        }

        return principal;
    }

    /**
     * An implementation of {@link PrivilegedExceptionAction} that obtains a
     * service ticket for an SPN.
     */
    static class GetServiceTicketAction implements PrivilegedExceptionAction<byte[]> {
        private String spn;

        public GetServiceTicketAction(String spn) {
            this.spn = spn;
        }

        public byte[] run() throws Exception {
            Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
            GSSManager manager = GSSManager.getInstance();
            GSSName serverName = manager.createName(spn, GSSName.NT_USER_NAME);
            GSSContext context = manager.createContext(serverName, krb5Oid, null, GSSContext.DEFAULT_LIFETIME);
            byte[] token = new byte[0];
            context.requestMutualAuth(false);
            context.requestCredDeleg(false);
            return context.initSecContext(token, 0, token.length);
        }
    }

    static class ValidateTicketAction implements PrivilegedExceptionAction<KrbTicket> {

        private boolean allowReplay = false;
        private byte[] ticket;

        ValidateTicketAction(byte[] ticket, boolean allowReplay) {
            this.ticket = ticket;
            this.allowReplay = allowReplay;
        }

        public KrbTicket run() throws Exception {
            GSSManager manager = GSSManager.getInstance();
            GSSContext context = manager.createContext((GSSCredential) null);
            try {
                context.acceptSecContext(ticket, 0, ticket.length);
            } catch (GSSException e) {
                Throwable cause = e.getCause();
                if (!(cause instanceof KrbApErrException) || ((KrbApErrException)cause).returnCode() != 34 || !allowReplay) {
                    throw e;
                }
            }
            return KrbTicket.newInstance(ticket, getCurrentSubject());
        }
    }

}
