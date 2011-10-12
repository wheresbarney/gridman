package org.gridman.coherence.security;

import com.tangosol.net.Invocable;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * An implementation of a {@link java.security.Permission} that
 * represents permissions for actions against a specific Coherence
 * Invocable.
 * <p/>
 * The action is held as a bit mask in a Java int.
 * <p/>
 *
 * @author Jonathan Knight
 */
public class InvocablePermission extends CoherencePermission {

    public InvocablePermission(Class<? extends Invocable> invocable, int mask) {
        super(invocable.getCanonicalName(), mask);
    }

    /**
     * Create a new permission for the specified target Invocable(s) and for the
     * specified actions(s).
     * <p/>
     * The target is either an astrix to represent all Invocables or a single Invocable name.
     * Invocable names may be suffixed with an astrix to represent wild-carded Invocable names.<b/>
     * For example<b/>
     * The target <code>test-*</code> matches all Invocable names that start with <code>test-</code>
     * <p/>
     * The actions can be a single actions or a comma-delimited list of actions. The actions names
     * must match valid names of actions in the {@link org.gridman.coherence.security.InvocableActions} class.
     * <p/>
     *
     * @param invocableName - the target Invocable this permission is for
     * @param actions - the actions this permission represents
     */
    public InvocablePermission(String invocableName, String actions) {
        super(invocableName, getActionMask(actions));
    }

    InvocablePermission(String resourceName, int mask) {
        super(resourceName, mask);
    }

    @Override
    protected int getAllActionMask() {
        return InvocableActions.ALL.getMask();
    }

    @Override
    protected int getNoneActionMask() {
        return InvocableActions.NONE.getMask();
    }

    /**
     * Returns a String representation of the actions represented by the
     * specified action mask. The action mask is a bit-mapped integer
     * where each bit represents a different action.
     * <p/>
     *
     * @param nAction - the action mask to convert to a string
     * @return a string representation of the specified action mask
     */
    public String formatAction(int nAction) {
        if (nAction == InvocableActions.ALL.getMask()) {
            return InvocableActions.ALL.getActionName();
        }

        if (nAction == InvocableActions.NONE.getMask()) {
            return InvocableActions.NONE.getActionName();
        }

        StringBuffer buff = new StringBuffer();
        for (InvocableActions invocableAction : InvocableActions.values()) {
            if (nAction == invocableAction.getMask()) {
                buff.append(',').append(invocableAction.getActionName());
            }
        }
        return buff.substring(1);
    }

    /**
     * Parse the specified comma-delimited list of actions and
     * set the actionMask field with the bit-mask representing
     * the listed actions.
     * <p/>
     *
     * @param actions - the comman-delimited list of actions.
     * @return the mask representing the specified actions
     * @throws IllegalArgumentException - if the actions parameter is null or an empty string
     */
    public static int getActionMask(String actions) {
        int mask = InvocableActions.NONE.mask;

        if (actions == null || actions.length() == 0) {
            throw new IllegalArgumentException("Action is not specified");
        }

        if (actions.equals(InvocableActions.ALL.getActionName())) {
            mask = InvocableActions.ALL.getMask();
        } else {
            StringTokenizer tokens = new StringTokenizer(actions.toLowerCase(), ",");
            while (tokens.hasMoreTokens()) {
                String sToken = tokens.nextToken().trim();
                InvocableActions invocableAction;
                invocableAction = InvocableActions.fromName(sToken.toUpperCase());
                mask |= invocableAction.getMask();
            }
        }

        return mask;
    }

    /**
     * Builds a map of all the possible InvocableActions and a corresponding
     * InvocablePermission instance for the specified invocable name.
     *
     * @param invocableName - the name of the invocable to build a InvocableAction/InvocablePermission map for
     * @return a map of all the possible InvocableActions and InvocablePermissions for the specified invocable.
     */
    public static Map<InvocableActions, InvocablePermission> buildPermissionMap(String invocableName) {
        Map<InvocableActions, InvocablePermission> map = new HashMap<InvocableActions, InvocablePermission>();
        for (InvocableActions action : InvocableActions.values()) {
            map.put(action, new InvocablePermission(invocableName, action.mask));
        }
        return map;
    }
}