package org.gridman.coherence.security.demo;

import org.apache.log4j.Logger;

import java.io.Serializable;
/**
 * This object is just meant to be a POJO.
 * Its a very simple class to represent a permissions cache.
 *
 * @todo - Add POF
 */
public class SecurityPermission implements Serializable {
    private static final Logger logger = Logger.getLogger(SecurityPermission.class);

    private static final long serialVersionUID = 1L;

    public static final String PERMISSION_CACHE = "PermissionCache";
    public static final String INVOKE_SERVICE = "InvokeService";    

    // @todo make these into an enum;
    public static final int PERMISSION_READ = 0;
    public static final int PERMISSION_WRITE = 1;
    public static final int PERMISSION_ADMIN = 2;
    public static final int PERMISSION_INVOKE = 3;

    private String role;
    private String resourceName;
    private int permission;

    public SecurityPermission() {}  // required by POF

    public SecurityPermission(String role, String resourceName, int permission) {
        this.role = role;
        this.resourceName = resourceName;
        this.permission = permission;
    }

    public String getRole() {
        return role;
    }

    public String getResourceName() {
        return resourceName;
    }

    public int getPermission() {
        return permission;
    }

    public boolean checkPermission(String checkResource, int perm, String myRole) {
        boolean result = role.equals(myRole) && permission == perm && checkResource.matches(resourceName);
        logger.info(toString() + ":" + result);
        return result;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityPermission that = (SecurityPermission) o;
        if (permission != that.permission) return false;
        if (!resourceName.equals(that.resourceName)) return false;
        if (!role.equals(that.role)) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = role.hashCode();
        result = 31 * result + resourceName.hashCode();
        result = 31 * result + permission;
        return result;
    }

    @Override public String toString() {
        return "org.gridman.coherence.security.demo.SecurityPermission{" +
                "role='" + role + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", permission=" + permission +
                '}';
    }
}
