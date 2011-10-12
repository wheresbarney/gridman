package org.gridman.coherence.security;

/**
 * @author Jonathan Knight
 */
public enum InvocableActions {
    NONE(0x00000000, "NONE", true),
    ALL(-1, "ALL", true),
    EXECUTE(0x00000001),
    QUERY(0x00000002);

    /**
     * The bit-map that represents the actions this InvocableActions implies.
     * Each bit of the int represents a different action.
     */
    public final int mask;

    /**
     * The name of this InvocableActions
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
    InvocableActions(InvocableActions... actions) {
        int flags = 0;
        for (InvocableActions action : actions) {
            flags |= action.mask;
        }
        this.mask = flags;
        composite = true;
    }

    /**
     * Construct a InvocableActions using the specified bit map.
     * <p/>
     *
     * @param flagValue - the bit map representing the actions that this InvocableActions implies
     */
    InvocableActions(int flagValue) {
        this(flagValue, null);
    }

    /**
     * Construct a InvocableActions with the specified bit map and
     * specified name.
     * <p/>
     *
     * @param flagValue - the bit-map representing the actions this InvocableActions implies.
     * @param name      - the name of this InvocableActions
     */
    InvocableActions(int flagValue, String name) {
        this(flagValue, name, false);
    }

    /**
     * Construct a InvocableActions with the specified
     * bit map, name and set the composite flag.
     * <p/>
     *
     * @param flagValue - the bit map of actions this InvocableActions implies
     * @param name      - the name of this InvocableActions
     * @param composite - true if this InvocableActions implies multiple actions, otherwise false
     */
    InvocableActions(int flagValue, String name, boolean composite) {
        this.mask = flagValue;
        this.name = name;
        this.composite = composite;
    }

    /**
     * Returns true if this InvocableActions is composed of, and hence implies
     * multiple single cache actions, otherwise returns false.
     * <p/>
     *
     * @return true if this InvocableActions is composed of, and hence implies
     *         multiple single cache actions, otherwise returns false.
     */
    public boolean isComposite() {
        return composite;
    }

    /**
     * Returns the bit-map mask that identifies the actions this InvocableActions
     * imples. Each bit of the int represents a different action. If the bit
     * is set (i.e. is equal to 1) then this InvocableActions imples the action
     * represented bu that bit.
     *
     * @return the bit-map mask that identifies the actions this InvocableActions
     *         imples.
     */
    public int getMask() {
        return mask;
    }

    /**
     * Returns the human readable name of this InvocableActions.
     * <p/>
     *
     * @return the human readable name of this InvocableActions.
     */
    public String getActionName() {
        return (name != null) ? name : this.toString().toLowerCase();
    }

    /**
     * Create a InvocableActions representing the specified action name.
     * <p/>
     *
     * @param name - the name of the InvocableActions to return
     * @return a InvocableActions representing the specified action name.
     * @throws IllegalArgumentException if the specified name does not represent a valid InvocableActions
     */
    public static InvocableActions fromName(String name) {
        for (InvocableActions action : values()) {
            if (action.getActionName().equalsIgnoreCase(name)) {
                return action;
            }
        }

        throw new IllegalArgumentException("name [" + name + "] does not match any InvocableActions name");
    }
}
