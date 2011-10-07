package org.gridman.coherence.security;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;
import org.gridman.coherence.security.simple.CoherenceSecurityUtils;
import org.gridman.security.permissions.PermissionChecker;

import javax.security.auth.Subject;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * @author Jonathan Knight
 */
public class PermissionedNamedCache implements NamedCache {

    private PermissionChecker provider;

    private Map<CacheActions,CachePermission> permissions;

    private NamedCache wrapped;
    
    public PermissionedNamedCache(NamedCache cache, PermissionChecker provider) {
        this.wrapped = cache;
        this.provider = provider;
        permissions = CachePermission.buildPermissionMap(cache.getCacheName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheName() {
        checkAccess(CacheActions.GETCACHENAME);
        return wrapped.getCacheName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheService getCacheService() {
        return wrapped.getCacheService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        checkAccess(CacheActions.ISACTIVE);
        return wrapped.isActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        checkAccess(CacheActions.RELEASE);
        wrapped.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        checkAccess(CacheActions.DESTROY);
        wrapped.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(Object oKey, Object oValue, long cMillis) {
        checkAccess(CacheActions.PUT);
        return wrapped.put(oKey, oValue, cMillis);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMapListener(MapListener listener) {
        checkAccess(CacheActions.ADDMAPLISTENER);
        wrapped.addMapListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMapListener(MapListener listener) {
        checkAccess(CacheActions.REMOVEMAPLISTENER);
        wrapped.removeMapListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMapListener(MapListener listener, Object oKey, boolean fLite) {
        checkAccess(CacheActions.ADDMAPLISTENER);
        wrapped.addMapListener(listener, oKey, fLite);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMapListener(MapListener listener, Object oKey) {
        checkAccess(CacheActions.REMOVEMAPLISTENER);
        wrapped.removeMapListener(listener, oKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMapListener(MapListener listener, Filter filter, boolean fLite) {
        checkAccess(CacheActions.ADDMAPLISTENER);
        wrapped.addMapListener(listener, filter, fLite);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMapListener(MapListener listener, Filter filter) {
        checkAccess(CacheActions.REMOVEMAPLISTENER);
        wrapped.removeMapListener(listener, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        checkAccess(CacheActions.SIZE);
        return wrapped.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        checkAccess(CacheActions.CLEAR);
        wrapped.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        checkAccess(CacheActions.ISEMPTY);
        return wrapped.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object oKey) {
        checkAccess(CacheActions.CONTAINSKEY);
        return wrapped.containsKey(oKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object oValue) {
        checkAccess(CacheActions.CONTAINSVALUE);
        return wrapped.containsValue(oValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection values() {
        checkAccess(CacheActions.VALUES);
        return wrapped.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map map) {
        checkAccess(CacheActions.PUTALL);
        wrapped.putAll(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set entrySet() {
        checkAccess(CacheActions.ENTRYSET);
        return wrapped.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set keySet() {
        checkAccess(CacheActions.KEYSET);
        return wrapped.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(Object oKey) {
        checkAccess(CacheActions.GET);
        return wrapped.get(oKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object remove(Object oKey) {
        checkAccess(CacheActions.REMOVE);
        return wrapped.remove(oKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(Object oKey, Object oValue) {
        checkAccess(CacheActions.PUT);
        return wrapped.put(oKey, oValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map getAll(Collection colKeys) {
        checkAccess(CacheActions.GETALL);
        return wrapped.getAll(colKeys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean lock(Object oKey, long cWait) {
        checkAccess(CacheActions.LOCK);
        return wrapped.lock(oKey, cWait);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean lock(Object oKey) {
        checkAccess(CacheActions.LOCK);
        return wrapped.lock(oKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unlock(Object oKey) {
        checkAccess(CacheActions.UNLOCK);
        return wrapped.unlock(oKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set keySet(Filter filter) {
        checkAccess(CacheActions.KEYSET);
        return wrapped.keySet(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set entrySet(Filter filter) {
        checkAccess(CacheActions.ENTRYSET);
        return wrapped.entrySet(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set entrySet(Filter filter, Comparator comparator) {
        checkAccess(CacheActions.ENTRYSET);
        return wrapped.entrySet(filter, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addIndex(ValueExtractor extractor, boolean fOrdered, Comparator comparator) {
        checkAccess(CacheActions.ADDINDEX);
        wrapped.addIndex(extractor, fOrdered, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeIndex(ValueExtractor extractor) {
        checkAccess(CacheActions.REMOVEINDEX);
        wrapped.removeIndex(extractor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Object oKey, EntryProcessor agent) {
        checkAccess(CacheActions.INVOKE);
        return wrapped.invoke(oKey, agent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map invokeAll(Collection collKeys, EntryProcessor agent) {
        checkAccess(CacheActions.INVOKEALL);
        return wrapped.invokeAll(collKeys, agent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map invokeAll(Filter filter, EntryProcessor agent) {
        checkAccess(CacheActions.INVOKEALL);
        return wrapped.invokeAll(filter, agent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object aggregate(Collection collKeys, EntryAggregator agent) {
        checkAccess(CacheActions.AGGREGATE);
        return wrapped.aggregate(collKeys, agent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object aggregate(Filter filter, EntryAggregator agent) {
        checkAccess(CacheActions.AGGREGATE);
        return wrapped.aggregate(filter, agent);
    }

    /**
     * Check that the current {@link javax.security.auth.Subject} associated with the
     * executing thread has permissions to invoke the specified action.
     *
     * @param action the action being invoked
     */
    private void checkAccess(CacheActions action) {
        Subject subject = CoherenceSecurityUtils.getCurrentSubject();
        provider.checkPermission(permissions.get(action), subject);
    }

}