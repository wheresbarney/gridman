package org.gridman.security.permissions;

import javax.security.auth.Subject;

/**
 * This class checks a given {@link javax.security.auth.Subject} to see if it qualifies for
 * an arbitrary permission. The qualifier does not require knowledge of the
 * permission is it only verifying a certain state of a given Subject.
 * <p/>
 * @author Jonathan Knight
 */
public interface PermissionQualifier {

    /**
     * Check the state of the given Subject to verify that
     * it qualifies for an arbitrary permission.
     *
     * @param subject - the subject to be verified
     * @return true if the Subject meets the required criteria, otherwise returns false
     */
    boolean qualifies(Subject subject);

}
