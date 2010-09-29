package org.gridman.kerberos;

import org.ietf.jgss.*;

import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.*;

/**
 * @author Jonathan Knight
 */
public class KrbHelper {
     private static final SecurityException DEFAULT_EXCEPTION = new SecurityException();

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
     * @param ticket the Kerberos session ticket to be validated
     * @return the user name from the ticket
     */
    public static KrbTicket validate(final byte[] ticket) {
        return doAs(new ValidateTicketAction(ticket));
    }

    /**
     * Run the specified {@link java.security.PrivilegedExceptionAction}
     * </p>
     * The {@link java.security.PrivilegedExceptionAction} will run within the context
     * of the {@link javax.security.auth.Subject} returned from calling
     * {@link KrbHelper} ensureCurrentSubject() method.
     *
     * @param action - the {@link java.security.PrivilegedExceptionAction} to run
     * @param <T> the return type of the {@link java.security.PrivilegedExceptionAction} run() method.
     * @return the value returned from the {@link java.security.PrivilegedExceptionAction} run() method.
     */
    public static <T> T doAs(PrivilegedExceptionAction<T> action) {
        return doAs(null, action);
    }

    /**
     * Run the specified {@link java.security.PrivilegedExceptionAction} in the
     * context of the specified {@link javax.security.auth.Subject}.
     * </p>
     * If the specified {@link javax.security.auth.Subject} is null
     * the {@link java.security.PrivilegedExceptionAction} will be run within the context
     * of the {@link javax.security.auth.Subject} returned from calling
     * {@link KrbHelper} ensureCurrentSubject() method.
     *
     * @param subject - the {@link javax.security.auth.Subject} to run the {@link java.security.PrivilegedAction} as
     * @param action - the {@link java.security.PrivilegedExceptionAction} to run
     * @param <T> the return type of the {@link java.security.PrivilegedExceptionAction} run() method.
     * @return the value returned from the {@link java.security.PrivilegedExceptionAction} run() method.
     */
    public static <T> T doAs(Subject subject, PrivilegedExceptionAction<T> action) {
        try {
            if (subject == null) {
                subject = ensureCurrentSubject();
            }

            return Subject.doAs(subject, action);
        } catch (PrivilegedActionException e) {
            throw ensureSecurityException(e);
        }
    }

    /**
     * Returns the {@link javax.security.auth.Subject} associated with the current
     * {@link Thread} or if none has been set logs on to the security context
     * configured in the JAAS configuration to obtain a {@link javax.security.auth.Subject}.
     *
     * @return the {@link javax.security.auth.Subject} associated to the current thread or if null,
     * the {@link javax.security.auth.Subject} obtained from the JAAS configuration.
     */
    public static Subject ensureCurrentSubject() {
        Subject subject = getCurrentSubject();
        if (subject == null) {
            subject = logon();
        }
        return subject;
    }

    /**
     * Logs on to the security system configured in the JAAS configuration.
     * </p>
     * The login module used is defined by the coherence.security.loginmodule System property
     * or if not set the module name defaults to Coherence.
     *
     * @return the Subject obtained from logging on using the configured login module.
     */
    public static Subject logon() {
        String moduleName = System.getProperty("coherence.security.loginmodule", "Coherence");
        String principal = System.getProperty("coherence.security.username");
        String password = System.getProperty("coherence.security.password");

        LoginContext lc = null;
        try {
            lc = new LoginContext(moduleName, new SimpleCallbackHandler(principal, password));
            lc.login();
        } catch (LoginException e) {
            throw ensureSecurityException(e);
        }
        return lc.getSubject();
    }

    /**
     * Return the Subject associated with the calling thread.
     *
     * @return the current Subject or null if a Subject is not associated with
     *         the calling thread
     */
    public static Subject getCurrentSubject() {
        Subject subject = null;
        Object context;

        SecurityManager manager = System.getSecurityManager();

        if (manager == null) {
            context = AccessController.getContext();
        } else {
            context = manager.getSecurityContext();
        }

        if (context != null && context instanceof AccessControlContext) {
            DomainCombiner dc = ((AccessControlContext) context).getDomainCombiner();
            if (dc != null && dc instanceof SubjectDomainCombiner) {
                subject = ((SubjectDomainCombiner) dc).getSubject();
            }
        }
        return subject;
    }

    public static SecurityException ensureSecurityException(Throwable throwable)
    {
      return ensureSecurityException(throwable, null);
    }

    /**
     * Convert the specified {@link Throwable} to a {@link SecurityException} with
     * the specified message.
     *
     * @param throwable - the {@link Throwable} to wrap in a {@link SecurityException}
     * @param message - the message for the {@link SecurityException}
     * @return A {@link SecurityException} with the specified message and causing exception
     */
    public static SecurityException ensureSecurityException(Throwable throwable, String message)
    {
      if (throwable == null)
      {
        return (message == null) ? DEFAULT_EXCEPTION : new SecurityException(message);
      }

      if ((throwable instanceof SecurityException) && (message == null))
      {
        return (SecurityException)throwable;
      }

      return new SecurityException(message, throwable);
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

        private byte[] ticket;

        ValidateTicketAction(byte[] ticket) {
            this.ticket = ticket;
        }

        public KrbTicket run() throws Exception {
            GSSManager manager = GSSManager.getInstance();
            GSSContext context = manager.createContext((GSSCredential) null);
            context.acceptSecContext(ticket, 0, ticket.length);
            return KrbTicket.newInstance(ticket, KrbHelper.getCurrentSubject());
        }
    }

    static class SimpleCallbackHandler implements CallbackHandler {
        private String username = null;
        private String password = null;

        public SimpleCallbackHandler(String pUsername, String pPassword) {
            username = pUsername;
            password = pPassword;
        }

        public void handle(Callback[] callbacks)
                throws java.io.IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    NameCallback nc = (NameCallback) callback;
                    nc.setName(username);
                } else if (callback instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback) callback;
                    pc.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
                }
            }
        }
    }

}
