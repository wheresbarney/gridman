package org.gridman.coherence.security.simple;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
/**
 * This object is just meant to be a POJO.
 * Its a very simple class to represent a permissions object in the cache.
 *
 * @todo - Add POF
 */
public class SimpleSecurityPermission implements Serializable, PortableObject {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(SimpleSecurityPermission.class);

    private String role;
    private String resourceName;
    private boolean cacheRatherThanInvoke;
    private boolean readOnly;

    public SimpleSecurityPermission(String role, String resourceName, boolean cacheRatherThanInvoke, boolean readOnly) {
        this.role = role;
        this.resourceName = resourceName;
        this.cacheRatherThanInvoke = cacheRatherThanInvoke;
        this.readOnly = readOnly;
    }

    public String getRole() {
        return role;
    }

    public String getResourceName() {
        return resourceName;
    }

    public boolean isCacheRatherThanInvoke() {
        return cacheRatherThanInvoke;
    }

    /**
     * write users can also read, we do regular expression matching...
     */
    public boolean matchPermission(String checkRole, String checkResourceName, boolean checkCacheRatherThanInvoke, boolean checkReadOnly) {
        logger.debug(toString() + " checkResourceName : " + checkResourceName);
        if(!role.equals(checkRole)|| checkCacheRatherThanInvoke!= cacheRatherThanInvoke) { return false; }
        if(!checkResourceName.matches(resourceName)) {return false; }
        if(!readOnly) { return true; }
        return checkReadOnly;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleSecurityPermission that = (SimpleSecurityPermission) o;
        if (cacheRatherThanInvoke != that.cacheRatherThanInvoke) return false;
        if (readOnly != that.readOnly) return false;
        if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null) return false;
        if (role != null ? !role.equals(that.role) : that.role != null) return false;
        return true;
    }

    @Override public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (resourceName != null ? resourceName.hashCode() : 0);
        result = 31 * result + (cacheRatherThanInvoke ? 1 : 0);
        result = 31 * result + (readOnly ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "SimpleSecurityPermission{" +
                "role='" + role + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", cacheRatherThanInvoke=" + cacheRatherThanInvoke +
                ", readOnly=" + readOnly +
                '}';
    }

    @Override public void readExternal(PofReader pofReader) throws IOException {
        role = pofReader.readString(0);
        resourceName = pofReader.readString(1);
        cacheRatherThanInvoke = pofReader.readBoolean(2);
        readOnly = pofReader.readBoolean(3);
    }

    @Override public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeString(0,role);
        pofWriter.writeString(1, resourceName);
        pofWriter.writeBoolean(2, cacheRatherThanInvoke);
        pofWriter.writeBoolean(3, readOnly);
    }
}
