package org.gridman.security;

import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.*;
import java.util.Collections;
import java.util.Set;

import static org.gridman.utils.CollectionUtils.asSet;

/**
 * @author Jonathan Knight
 */
public class JaasHelper {
    public static final String PROP_JAAS_MODULE = "gridman.security.loginmodule";
    private static final SecurityException DEFAULT_EXCEPTION = new SecurityException();

    /**
     * Run the specified {@link java.security.PrivilegedExceptionAction}
     * </p>
     * The {@link java.security.PrivilegedExceptionAction} will run within the context
     * of the {@link javax.security.auth.Subject} returned from calling
     * {@link #ensureCurrentSubject} method.
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
     * {@link #ensureCurrentSubject} method.
     *
     * @param subject - the {@link javax.security.auth.Subject} to run the {@link java.security.PrivilegedAction} as
     * @param action - the {@link java.security.PrivilegedExceptionAction} to run
     * @param <T> the return type of the {@link java.security.PrivilegedExceptionAction} run() method.
     * @return the value returned from the {@link java.security.PrivilegedExceptionAction} run() method.
     */
    public static <T> T doAs(Subject subject, PrivilegedExceptionAction<T> action) {
        try {
            if (subject == null) {
                subject = getCurrentSubject();
            }

            return Subject.doAs(subject, action);
        } catch (PrivilegedActionException e) {
            Throwable cause = e.getCause();
            throw ensureSecurityException((cause != null) ? cause : e);
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
        String moduleName = System.getProperty(PROP_JAAS_MODULE, "GridMan");
        return logon(moduleName);
    }

    /**
     * Logs on to the security system configured in the JAAS configuration.
     * </p>
     * The specified login module be used
     * to log on and obtain a {@link javax.security.auth.Subject}.
     *
     * @param moduleName - the name of the login module to use in the JAAS configuration
     * @return the Subject obtained from logging on using the configured login module.
     */
    public static Subject logon(String moduleName) {
        return logon(moduleName, null, null);
    }

    /**
     * Logs on to the security system configured in the JAAS configuration.
     * </p>
     * The specified login module, principal name and password will be used
     * to log on and obtain a {@link javax.security.auth.Subject}.
     *
     * @param moduleName - the name of the login module to use in the JAAS configuration
     * @param principal - the principal name to use to log on
     * @param password - the password to use to verify the principal
     *
     * @return the Subject obtained from logging on using the configured login module.
     */
    public static Subject logon(String moduleName, String principal, String password) {
        LoginContext lc;
        try {
            lc = new LoginContext(moduleName, new GridManCallbackHandler(principal, password));
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

    /**
     * Convert the specified {@link Throwable} to a {@link SecurityException}.
     * </p>
     * @param throwable - the {@link Throwable} to wrap in a {@link SecurityException}
     * @return A {@link SecurityException} with the specified causing exception
     */
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

    public static String getFirstPrincipalName(Subject subject) {
        if(subject == null) { return null; }
        return subject.getPrincipals().iterator().next().getName();
    }

    public static <T extends Principal> T getFirstPrincipal(Subject subject, Class<T> type) {
        if(subject == null) { return null; }
        Set<T> principals = subject.getPrincipals(type);
        return (!principals.isEmpty()) ? principals.iterator().next() : null;
    }


    public static Subject asSubject(Principal... principals) {
        return new Subject(false, asSet(principals), Collections.emptySet(), Collections.emptySet());
    }
    
}
