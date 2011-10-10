package org.gridman.coherence.security;

/**
 * This enum represents different actions that can be performed on a cache.
 * @author Jonathan Knight
 */
public enum CacheActions {
    NONE(0x00000000, "NONE", true),
    ALL(-1, "ALL", true),
    ADDINDEX(0x00000001),
    ADDMAPLISTENER(0x00000002),
    AGGREGATE(0x00000004),
    CLEAR(0x00000008),
    CONTAINSKEY(0x00000010),
    CONTAINSVALUE(0x00000020),
    DESTROY(0x00000040),
    ENTRYSET(0x00000080),
    GET(0x00000100),
    GETALL(0x00000200),
    GETCACHENAME(0x00000400),
    GETCACHESERVICE(0x00000800),
    INVOKE(0x00001000),
    INVOKEALL(0x00002000),
    ISEMPTY(0x00004000),
    KEYSET(0x00008000),
    LOCK(0x00010000),
    PUT(0x00020000),
    PUTALL(0x00040000),
    RELEASE(0x00080000),
    REMOVE(0x00100000),
    REMOVEINDEX(0x00200000),
    REMOVEMAPLISTENER(0x00400000),
    SIZE(0x00800000),
    UNLOCK(0x01000000),
    VALUES(0x02000000),
    ENSURE(0x04000000),
    ISACTIVE(0x08000000),
    CONNECT(ENSURE, GETCACHENAME, GETCACHESERVICE, RELEASE),
    READ(CONNECT, ADDINDEX, ADDMAPLISTENER, AGGREGATE, CONTAINSKEY, CONTAINSVALUE, ENTRYSET
            , GET, GETALL, ISEMPTY, KEYSET, REMOVEINDEX, REMOVEMAPLISTENER, SIZE, VALUES),
    WRITE(READ, AGGREGATE, CLEAR, DESTROY, LOCK, PUT, PUTALL, REMOVE, UNLOCK),
    EXECUTE(INVOKE, INVOKEALL);

    /**
     * The bit-map that represents the actions this CacheActions implies.
     * Each bit of the int represents a different action.
     */
    public final int mask;
    /**
     * The name of this CacheActions
     */
    private String name;
    /**
     * A flag indicating that this action imples multiple other actions
     */
    private boolean composite = false;

    /**
     * A constructor to build composite actions.
     * <p/>
     *
     * @param actions the actions that this CacheAvtion will imply
     */
    CacheActions(CacheActions... actions) {
        int flags = 0;
        for (CacheActions action : actions) {
            flags |= action.mask;
        }
        this.mask = flags;
        composite = true;
    }

    /**
     * Construct a CacheActions using the specified bit map.
     * <p/>
     *
     * @param flagValue - the bit map representing the actions that this CacheActions implies
     */
    CacheActions(int flagValue) {
        this(flagValue, null);
    }

    /**
     * Construct a CacheActions with the specified bit map and
     * specified name.
     * <p/>
     *
     * @param flagValue - the bit-map representing the actions this CacheActions implies.
     * @param name      - the name of this CacheActions
     */
    CacheActions(int flagValue, String name) {
        this(flagValue, name, false);
    }

    /**
     * Construct a CacheActions with the specified
     * bit map, name and set the composite flag.
     * <p/>
     *
     * @param flagValue - the bit map of actions this CacheActions implies
     * @param name      - the name of this CacheActions
     * @param composite - true if this CacheActions implies multiple actions, otherwise false
     */
    CacheActions(int flagValue, String name, boolean composite) {
        this.mask = flagValue;
        this.name = name;
        this.composite = composite;
    }

    /**
     * Returns true if this CacheActions is composed of, and hence implies
     * multiple single cache actions, otherwise returns false.
     * <p/>
     *
     * @return true if this CacheActions is composed of, and hence implies
     *         multiple single cache actions, otherwise returns false.
     */
    public boolean isComposite() {
        return composite;
    }

    /**
     * Returns the bit-map mask that identifies the actions this CacheActions
     * imples. Each bit of the int represents a different action. If the bit
     * is set (i.e. is equal to 1) then this CacheActions imples the action
     * represented bu that bit.
     *
     * @return the bit-map mask that identifies the actions this CacheActions
     *         imples.
     */
    public int getMask() {
        return mask;
    }

    /**
     * Returns the human readable name of this CacheActions.
     * <p/>
     *
     * @return the human readable name of this CacheActions.
     */
    public String getActionName() {
        return (name != null) ? name : this.toString().toLowerCase();
    }

    /**
     * Create a CacheActions representing the specified action name.
     * <p/>
     *
     * @param name - the name of the CacheActions to return
     * @return a CacheActions representing the specified action name.
     * @throws IllegalArgumentException if the specified name does not represent a valid CacheActions
     */
    public static CacheActions fromName(String name) {
        for (CacheActions action : CacheActions.values()) {
            if (action.getActionName().equalsIgnoreCase(name)) {
                return action;
            }
        }

        throw new IllegalArgumentException("name [" + name + "] does not match any CacheActions name");
    }
}