package org.gridman.security.permissions;

import org.gridman.security.JaasHelper;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * An instance of a {@link PermissionQualifier} that qualifies a given
 * {@link javax.security.auth.Subject} if that Subject contains a {@link java.security.Principal} of a
 * specific type and that Principal has a specific name.
 *
 * @author Jonathan Knight
 */
public class PrincipalNameQualifier implements PermissionQualifier {

    /** The Principal type that the Subject must contain */
    private Class<? extends Principal> principalType;

    /** The qualifying name of the Principal */
    private String name;

    public PrincipalNameQualifier(Class<? extends Principal> principalType, String name) {
        if (principalType == null) {
            throw new IllegalArgumentException("Principal Type cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Principal Name cannot be null");
        }

        this.principalType = principalType;
        this.name = name;
    }

    @Override
    public boolean qualifies(Subject subject) {
        Principal principal = JaasHelper.getFirstPrincipal(subject, principalType);
        return principal != null && name.equals(principal.getName());
    }

    public Class<? extends Principal> getPrincipalType() {
        return principalType;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PrincipalNameQualifier that = (PrincipalNameQualifier) o;

        return name.equals(that.name) && principalType.equals(that.principalType);
    }

    @Override
    public int hashCode() {
        int result = principalType.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
