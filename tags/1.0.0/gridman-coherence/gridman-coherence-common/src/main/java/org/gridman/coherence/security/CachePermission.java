package org.gridman.coherence.security;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

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
public class CachePermission extends CoherencePermission {

    /**
     * Create a new permission for the specified target cache(s) and for the
     * specified actions(s).
     * <p/>
     * The target is either an astrix to represent all caches or a single cache name.
     * Cache names may be suffixed with an astrix to represent wild-carded cache names.<b/>
     * For example<b/>
     * The target <code>test-*</code> matches all cache names that start with <code>test-</code>
     * <p/>
     * The actions can be a single actions or a comma-delimited list of actions. The actions names
     * must match valid names of actions in the {@link org.gridman.coherence.security.CacheActions} class.
     * <p/>
     *
     * @param cacheName - the target cache this permission is for
     * @param actions - the actions this permission represents
     */
    public CachePermission(String cacheName, String actions) {
        super(cacheName, getActionMask(actions));
    }

    CachePermission(String resourceName, int mask) {
        super(resourceName, mask);
    }

    @Override
    protected int getAllActionMask() {
        return CacheActions.ALL.getMask();
    }

    @Override
    protected int getNoneActionMask() {
        return CacheActions.NONE.getMask();
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
        if (nAction == CacheActions.ALL.getMask()) {
            return CacheActions.ALL.getActionName();
        }

        if (nAction == CacheActions.NONE.getMask()) {
            return CacheActions.NONE.getActionName();
        }

        StringBuffer buff = new StringBuffer();
        for (CacheActions cacheAction : CacheActions.values()) {
            if (nAction == cacheAction.getMask()) {
                buff.append(',').append(cacheAction.getActionName());
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
        int mask = CacheActions.NONE.mask;

        if (actions == null || actions.length() == 0) {
            throw new IllegalArgumentException("Action is not specified");
        }

        if (actions.equals(CacheActions.ALL.getActionName())) {
            mask = CacheActions.ALL.getMask();
        } else {
            StringTokenizer tokens = new StringTokenizer(actions.toLowerCase(), ",");
            while (tokens.hasMoreTokens()) {
                String sToken = tokens.nextToken().trim();
                CacheActions cacheAction;
                cacheAction = CacheActions.fromName(sToken.toUpperCase());
                mask |= cacheAction.getMask();
            }
        }

        return mask;
    }

    /**
     * Builds a map of all the possible CacheActions and a corresponding
     * CachePermission instance for the specified cache name.
     *
     * @param cacheName - the name of the cache to build a CacheAction/CachePermission map for
     * @return a map of all the possible CacheActions and CachePermissions for the specified cache.
     */
    public static Map<CacheActions,CachePermission> buildPermissionMap(String cacheName) {
        Map<CacheActions,CachePermission> map = new HashMap<CacheActions,CachePermission>();
        for (CacheActions action : CacheActions.values()) {
            map.put(action, new CachePermission(cacheName, action.mask));
        }
        return map;
    }
}

