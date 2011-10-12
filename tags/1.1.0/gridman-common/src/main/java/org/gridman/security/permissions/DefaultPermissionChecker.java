package org.gridman.security.permissions;

import javax.security.auth.Subject;
import java.security.Permission;
import java.util.*;

/**
 * An implementation of a {@link org.gridman.security.permissions.PermissionChecker} that can verify a
 * Subject qualifies for a given {@link java.security.Permission}.
 * <p/>
 * This implementation uses the concept of a set of {@link PermissionQualifier}s that
 * can determine if a given Subject qualifies for a Permission. A set of PermissionQualifier
 * instances is registered against Permission instances. When a check is made for a requested
 * permission The PermissionQualifiers of any registered Permission instances that imply
 * the requested Permission are checked to see if the Subject meets their qualification
 * criteria.
 *
 * @author Jonathan Knight
 */
public class DefaultPermissionChecker implements PermissionChecker {

    /** The map of Permissions and corresponding qualifiers for those permissions */
    private Map<Permission, Set<PermissionQualifier>> permissionQualifiers;

    /**
     * If true any requested permission that has no implied qualifiers always is authorised
     * for any Subject. If set to false then any any requested permission with no implied
     * qualifiers is never authorised.
     */
    private boolean noQualifiersQualifiesAny = true;

    public DefaultPermissionChecker() {
        permissionQualifiers = new HashMap<Permission, Set<PermissionQualifier>>();
    }

    public Map<Permission, Set<PermissionQualifier>> getPermissionQualifiers() {
        return Collections.unmodifiableMap(permissionQualifiers);
    }

    public boolean noQualifiersQualifiesAny() {
        return noQualifiersQualifiesAny;
    }

    public void setNoQualifiersQualifiesAny(boolean noQualifiersQualifiesAny) {
        this.noQualifiersQualifiesAny = noQualifiersQualifiesAny;
    }

    public void addPermissionQualifier(Permission permission, PermissionQualifier qualifier) {
        Set<PermissionQualifier> qualifiers = permissionQualifiers.get(permission);
        if (qualifiers == null) {
            qualifiers = new HashSet<PermissionQualifier>();
            permissionQualifiers.put(permission, qualifiers);
        }
        qualifiers.add(qualifier);
    }

    /**
     * Verify that the specified {@link javax.security.auth.Subject} is authorised to perform
     * the requested {@link java.security.Permission}.
     *
     * @param requestedPermission - the permission being requested
     * @param subject the Subject being authorised
     *
     * @throws SecurityException if the Subject os not authorised to perform the
     *         requested permission.
     */
    @Override
    public void checkPermission(Permission requestedPermission, Subject subject) {
        boolean qualifies = false;

        Set<PermissionQualifier> qualifiers = getImpylingPermissionQualifiers(requestedPermission);
        if (!qualifiers.isEmpty()) {
            Iterator<PermissionQualifier> it = qualifiers.iterator();
            while(!qualifies && it.hasNext()) {
                PermissionQualifier qualifier = it.next();
                qualifies = qualifier.qualifies(subject);
            }
        } else {
            qualifies = noQualifiersQualifiesAny;
        }

        if (!qualifies) {
            throw new SecurityException("Not authorised for permission " + requestedPermission);
        }
    }

    /**
     * Returns the list of qualifiers that can imply qualification of a {@link javax.security.auth.Subject} for
     * the requested {@link java.security.Permission}.
     *
     * @param requestedPermission - the permission being requested
     * @return the set of PermissionQualifiers that may qualify a Subject for a Permission
     */
    public Set<PermissionQualifier> getImpylingPermissionQualifiers(Permission requestedPermission) {
        Set<PermissionQualifier> qualifiers = new HashSet<PermissionQualifier>();
        for (Map.Entry<Permission, Set<PermissionQualifier>> entry : permissionQualifiers.entrySet()) {
            if (entry.getKey().implies(requestedPermission)) {
                qualifiers.addAll(entry.getValue());
            }
        }
        return qualifiers;
    }

    /**
     * Returns the Set of qualifiers that qualify a {@link javax.security.auth.Subject} for
     * the requested {@link java.security.Permission}.
     *
     * @param requestedPermission - the permission being requested
     * @return the set of PermissionQualifiers that may qualify a Subject for a Permission
     */
    public Set<PermissionQualifier> getPermissionQualifiers(Permission requestedPermission) {
        Set<PermissionQualifier> qualifiers = new HashSet<PermissionQualifier>();
        if (permissionQualifiers.containsKey(requestedPermission)) {
            qualifiers.addAll(permissionQualifiers.get(requestedPermission));
        }
        return qualifiers;
    }

}
