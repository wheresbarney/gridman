package org.gridman.coherence.security;

import com.tangosol.util.Base;

import java.security.Permission;

/**
 * An implementation of a {@link java.security.Permission} that
 * represents permissions for actions against a specific Coherence
 * cache.
 * <p/>
 * The action is held as a bit mask in a Java int.
 * <p/>
 *
 * @author Jonathan Knight
 */
public abstract class CoherencePermission extends Permission {
    public static final char RECURSIVE_CHAR = '*';

    /** The action this permission is for */
    private transient int actionMask;

    /** The name of the cache this permission applies to */
    private transient String resourceName;

    /** is a recursive permission */
    private transient boolean recursive;

    CoherencePermission(String resourceName, int mask) {
        super(resourceName);
        init(mask);
    }

    private void init(int mask) {
        if ((getAllActionMask() & mask) != mask) {
            throw new IllegalArgumentException("Invalid actions mask");
        }

        if (getNoneActionMask() == mask) {
            throw new IllegalArgumentException("Invalid actions mask");
        }

        this.actionMask = mask;

        resourceName = getName();
        int len = resourceName.length();
        char last = ((len > 0) ? resourceName.charAt(len - 1) : 0);
        if (last == RECURSIVE_CHAR) {
            recursive = true;
            resourceName = resourceName.substring(0, --len);
        }
    }

    /**
     * Gets the action mask that represents ALL actions
     * @return the action mask that represents ALL actions
     */
    protected abstract int getAllActionMask();

    /**
     * Gets the action mask that represents NONE actions
     * @return the action mask that represents NONE actions
     */
    protected abstract int getNoneActionMask();

    /**
     * Gets the String representation of the given action mask
     *
     * @param mask - the mask to convert to a String
     * @return the String representation of the given action mask
     */
    protected abstract String formatAction(int mask);

    public int getActionMask() {
        return actionMask;
    }

    /**
     * Returns the actions as a String. This is abstract
     * so subclasses can defer creating a String representation until
     * one is needed. Subclasses should always return actions in what they
     * consider to be their
     * canonical form. For example, two FilePermission objects created via
     * the following:
     * <p/>
     * <pre>
     *   perm1 = new FilePermission(p1,"read,write");
     *   perm2 = new FilePermission(p2,"write,read");
     * </pre>
     * <p/>
     * both return
     * "read,write" when the <code>getActions</code> method is invoked.
     *
     * @return the actions of this Permission.
     */
    public String getActions() {
        return formatAction(this.actionMask);
    }

    /**
     * Checks if the specified permission's actions are "implied by"
     * this object's actions.
     * <p/>
     * This must be implemented by subclasses of Permission, as they are the
     * only ones that can impose semantics on a Permission object.
     * <p/>
     * <p>The <code>implies</code> method is used by the AccessController to determine
     * whether or not a requested permission is implied by another permission that
     * is known to be valid in the current execution context.
     *
     * @param permission the permission to check against.
     * @return true if the specified permission is implied by this object,
     *         false if not.
     */
    public boolean implies(Permission permission) {
        if (!(permission instanceof CoherencePermission)) {
            return false;
        }

        CoherencePermission that = (CoherencePermission) permission;
        return ((this.actionMask & that.actionMask) == that.actionMask) && impliesIgnoreMask(that);
    }

    boolean impliesIgnoreMask(CoherencePermission that) {
        boolean implies;
        if (this.recursive) {
            implies = (that.resourceName.length() >= this.resourceName.length()) && that.resourceName.startsWith(this.resourceName);
        } else {
            implies = this.resourceName.equals(that.resourceName);
        }
        return implies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CoherencePermission) {
            CoherencePermission that = (CoherencePermission) obj;
            return ((Base.equals(getName(), that.getName())) && (this.actionMask == that.actionMask));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + actionMask;
        return result;
    }

}