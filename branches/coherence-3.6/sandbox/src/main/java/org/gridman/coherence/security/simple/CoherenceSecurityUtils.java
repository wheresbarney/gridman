package org.gridman.coherence.security.simple;

import com.tangosol.io.pof.PofPrincipal;

import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.DomainCombiner;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Useful static functions that sit here for now.
 */
public class CoherenceSecurityUtils {

    /**
     * Gets the Subject from the current thread, or null if there isn't one.     
     */
    public static Subject getCurrentSubject() {
        SecurityManager manager = System.getSecurityManager();
        Object oContext = (manager == null) ? AccessController.getContext() : manager.getSecurityContext();
        if (oContext instanceof AccessControlContext) {
            DomainCombiner dC = ((AccessControlContext) oContext).getDomainCombiner();
            if (dC instanceof SubjectDomainCombiner) {
                return ((SubjectDomainCombiner) dC).getSubject();
            }
        }
        return null;
    }

    public static boolean checkFirstPrincipalName(String name) {
        return name.equals(getCurrentSubject().getPrincipals().iterator().next().getName());        
    }

    public static Subject getSimpleSubject(String name) {
        return new Subject(true, new HashSet(Arrays.asList(new PofPrincipal(name))),new HashSet(),new HashSet());
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

    public static String getCurrentFirstPrincipalName() {
        return getFirstPrincipalName(getCurrentSubject());
    }
}
