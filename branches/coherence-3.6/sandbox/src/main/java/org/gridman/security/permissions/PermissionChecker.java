package org.gridman.security.permissions;

import javax.security.auth.Subject;
import java.security.Permission;

/**
 * @author Jonathan Knight
 */
public interface PermissionChecker {

    /**
     * Verify that the specified {@link Subject} is authorised to perform
     * the requested {@link Permission}.
     *
     * @param requestedPermission - the permission being requested
     * @param subject the Subject being authorised
     *
     * @throws SecurityException if the Suject os not authorised to perform the
     *         requested permission.
     */
    void checkPermission(Permission requestedPermission, Subject subject);
    
}
