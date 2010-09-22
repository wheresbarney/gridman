package org.gridman.coherence.security.simple;

import com.tangosol.io.pof.PofPrincipal;

import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.DomainCombiner;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Useful static functions that sit here for now.
 */
public class CoherenceUtils {

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
}
