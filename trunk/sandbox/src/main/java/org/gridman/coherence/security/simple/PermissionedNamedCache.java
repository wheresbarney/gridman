package org.gridman.coherence.security.simple;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.WrapperNamedCache;

import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.MapListener;

import com.tangosol.util.ValueExtractor;

import java.security.AccessControlContext;
import java.security.AccessController;

import java.security.DomainCombiner;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;

import org.apache.log4j.Logger;

public class PermissionedNamedCache extends WrapperNamedCache {
    private static final Logger logger = Logger.getLogger(PermissionedNamedCache.class);

    private CacheSecurityProvider provider;
    private String cacheName;

    public PermissionedNamedCache(NamedCache cache, CacheSecurityProvider provider) {
        super(cache, cache.getCacheName());
        cacheName = cache.getCacheName();
        this.provider = provider;
    }

    /**
     * The method checks if the request to find the cache name is a valid 
     * user thread before returning the cache name.
     * @return String Cache name if request is valid
     * @since 04/05/2008
     */
    public String getCacheName() {
        checkAccess(true);
        return super.getCacheName();
    }

    /**
     * The method checks if the request for the CacheService is from a valid 
     * user thread before returnung the CacheService.
     * @return CacheService
     * @since 04/05/2008
     */
    public CacheService getCacheService() {
        checkAccess(true);
        return super.getCacheService();
    }

    /**
     * The method checks if the request to find if the cache is still active 
     * comes from a valid user thread before returing if the cache is active.
     * @return boolean true if the Cache is active otherwise false
     * @since 04/05/2008
     */
    public boolean isActive() {
        checkAccess(true);
        return super.isActive();
    }

    /**
     * The method releases the cache if the request comes from a valid user 
     * thread.
     * @since 04/05/2008
     */
    public void release() {
        checkAccess(false);
        super.release();
    }

    /**
     * The method destroys the cache if the request comes from a valid user 
     * thread.
     * @since 04/05/2008
     */
    public void destroy() {
        checkAccess(false);
        super.destroy();
    }

    /**
     * The method puts the key and value with the passed time-to-live after
     * checking if the request is made from a valid user thread.
     * @param oKey Object Map Key
     * @param oValue Object Map Value
     * @param cMillis long Time to Live
     * @return Object The previous value of the oKey, null if none was stored.
     * @since 04/05/2008
     */
    public Object put(Object oKey, Object oValue, long cMillis) {
        checkAccess(false);
        return super.put(oKey, oValue, cMillis);
    }

    /**
     * The method allows to add a MapListener to the cache if the request is
     * made from a valid user thread.
     * @param listener MapListener
     * @since 04/05/2008
     */
    public void addMapListener(MapListener listener) {
        checkAccess(true);
        super.addMapListener(listener);
    }

    /**
     * The method allows to remove a MapListener from the Cache if the request
     * comes from a valid user thread.
     * @param listener MapListener
     * @since 04/05/2008
     */
    public void removeMapListener(MapListener listener) {
        checkAccess(true);
        super.removeMapListener(listener);
    }

    /**
     * The method adds a MapListener to the cache for a given key and with a 
     * flag to send a lite or a complete object after checking if the request
     * is being made from a valid user thread.
     * @param listener MapLietener
     * @param oKey Object A Key to attach a Listener for
     * @param fLite boolean true to send a lite message
     * @since 04/05/2008
     */
    public void addMapListener(MapListener listener, Object oKey, 
                               boolean fLite) {
        checkAccess(true);
        super.addMapListener(listener, oKey, fLite);
    }

    /**
     * The method revomes a MapListener attached for a given key if the request
     * is made from a valid user thread.
     * @param listener MapListener
     * @param oKey Object The key the Listener is attached for.
     * @since 04/05/2008
     */
    public void removeMapListener(MapListener listener, Object oKey) {
        checkAccess(true);
        super.removeMapListener(listener, oKey);
    }

    /**
     * The method adds a MapListener for a given Filter with a flag to either 
     * send lite events or complete message with old and new values if the 
     * request is made from a valid user thread.
     * @param listener MapListener
     * @param filter Filter
     * @param fLite boolean true if an event is lite
     * @since 04/05/2008
     */
    public void addMapListener(MapListener listener, Filter filter, 
                               boolean fLite) {
        checkAccess(true);
        super.addMapListener(listener, filter, fLite);
    }

    /**
     * The method removes the MapListener for a given Filter if the request is
     * made from a valid user thread.
     * @param listener
     * @param filter
     * @since 04/05/2008
     */
    public void removeMapListener(MapListener listener, Filter filter) {
        checkAccess(true);
        super.removeMapListener(listener, filter);
    }

    /**
     * The method returns the size of the Cache if the request is made from a 
     * valid user thread.
     * @return int The number of elements stored in the Cache.
     * @since 04/05/2008
     */
    public int size() {
        checkAccess(true);
        return super.size();
    }

    /**
     * The method clears the cache is the request is made from a valid user 
     * thread.
     * @since 04/05/2008
     */
    public void clear() {
        checkAccess(false);
        super.clear();
    }

    /**
     * The method returns if the cache is empty or not if the request is made 
     * from a valid user thread.
     * @return boolean true if the cache is empty.
     * @since 04/05/2008
     */
    public boolean isEmpty() {
        checkAccess(true);
        return super.isEmpty();
    }

    /**
     * The method checks if the passed key exist in the Cache if the request is
     * made from a valid user thread.
     * @param oKey Object The key to find if the Object exist in the Cache.
     * @return boolean true if the value exist in the Cache.
     * @since 04/05/2008
     */
    public boolean containsKey(Object oKey) {
        checkAccess(true);
        return super.containsKey(oKey);
    }

    /**
     * The method checks if the Value exist in the Cache if the request is made 
     * from a valid user thread.
     * @param oValue Object Cache Value to see if it exist  in the Cache.
     * @return boolean true if the Value exist in the cache.
     * @since 04/05/2008
     */
    public boolean containsValue(Object oValue) {
        checkAccess(true);
        return super.containsValue(oValue);
    }

    /**
     * The method returns a Collection of values in the cache if the request is
     * made from a valid user thread.
     * @return Collection Values in the Cache.
     * @since 04/05/2008
     */
    public Collection values() {
        checkAccess(true);
        return super.values();
    }

    /**
     * The method puts elements in the passed Map in the Cache in parallel if
     * the request is made from a valid user thread.
     * @param map Map
     * @since 04/05/2008
     */
    public void putAll(Map map) {
        checkAccess(false);
        super.putAll(map);
    }

    /**
     * The method returns a Set of all entries in the Cache if the request is 
     * made from a valid user thread.
     * @return Set Entry set
     * @since 04/05/2008
     */
    public Set entrySet() {
        checkAccess(true);
        return super.entrySet();
    }

    /**
     * The method returns a Set of all the keys in the Cache if the request is 
     * made from a valid user thread.
     * @return Set A Set of all the Cache keys
     * @since 04/05/2008
     */
    public Set keySet() {
        checkAccess(true);
        return super.keySet();
    }

    /**
     * The method returns the value of the passed Cache key if the request is 
     * made from a valid user thread.
     * @param oKey Object Cache key to get the value for
     * @return Object the Value of the cache key
     * @since 04/05/2008
     */
    public Object get(Object oKey) {
        checkAccess(true);
        return super.get(oKey);
    }

    /**
     * The method removes an entry from the cache for the passed key if the 
     * request is made from a valid user thread.
     * @param oKey Object The key to remove the Cache entry for
     * @return Object the last value of the key removed.
     * @since 04/05/2008
     */
    public Object remove(Object oKey) {
        checkAccess(false);
        return super.remove(oKey);
    }

    /**
     * The method puts a Cache entry if the request is made from a valid user
     * thread.
     * @param oKey Object key
     * @param oValue Object value
     * @return Object the last value for the key
     * @since 04/05/2008
     */
    public Object put(Object oKey, Object oValue) {
        checkAccess(false);
        return super.put(oKey, oValue);
    }

    /**
     * The method returns a Map of all the key, value pair for the passed
     * collection of cache keys if requested by a valid user thread.
     * @param colKeys Collection of Cache keys
     * @return Map
     * @since 04/05/2008
     */
    public Map getAll(Collection colKeys) {
        checkAccess(true);
        return super.getAll(colKeys);
    }

    /**
     * The method locks a cache key for a specified time in millisecond if the 
     * request is made from a valid user thread.
     * @param oKey Object Key to lock
     * @param cWait Time to expire the lock in ms
     * @return boolean true if the lock has been successfully acquired.
     * @since 04/05/2008
     */
    public boolean lock(Object oKey, long cWait) {
        checkAccess(false);
        return super.lock(oKey, cWait);
    }

    /**
     * The method locks a Cache key if the request is made from a valid user
     * thread.
     * @param oKey Object key to lock
     * @return boolean true if the lock has been successfully acquired
     * @since 04/05/2008
     */
    public boolean lock(Object oKey) {
        checkAccess(false);
        return super.lock(oKey);
    }

    /**
     * The method unlocks a cache key if previously acquired if the request is 
     * made from a valid user thread
     * @param oKey Object Key
     * @return boolean true if the lock was successfully unlocked
     * @since 04/05/2008
     */
    public boolean unlock(Object oKey) {
        checkAccess(false);
        return super.unlock(oKey);
    }

    /**
     * The method returns a Set of cache keys that satisy the passed filter if
     * the request is made from a valid user thread
     * @param filter
     * @return Set set of cache keys
     * @since 04/05/2008
     */
    public Set keySet(Filter filter) {
        checkAccess(true);
        return super.keySet(filter);
    }

    /**
     * The method returns a set of entries for a given filter if the request is
     * made from a valid user thread.
     * @param filter
     * @return Set set of entries 
     * @since 04/05/2008
     */
    public Set entrySet(Filter filter) {
        checkAccess(true);
        return super.entrySet(filter);
    }

    /**
     * The method returns an Entry set for a given Filter and a Comparator if
     * the request is made from a valid user thread.
     * @param filter Filter
     * @param comparator Comparator
     * @return Set of Entries in the cache
     * @since 04/05/2008
     */
    public Set entrySet(Filter filter, Comparator comparator) {
        checkAccess(true);
        return super.entrySet(filter, comparator);
    }

    /**
     * The method adds an Index for a given Value extractor and the Comparator
     * and if it needs to be ordered for a valid user thread.
     * @param extractor ValueExtractor
     * @param fOrdered boolean if Indexes are ordered
     * @param comparator Comparator A custom Comparator
     * @since 04/05/2008
     */
    public void addIndex(ValueExtractor extractor, boolean fOrdered, 
                         Comparator comparator) {
        checkAccess(false);
        super.addIndex(extractor, fOrdered, comparator);
    }

    /**
     * The method removes an Index for a given ValueExtractor for a valid user
     * thread
     * @param extractor ValueExtractor
     * @since 04/05/2008
     */
    public void removeIndex(ValueExtractor extractor) {
        checkAccess(false);
        super.removeIndex(extractor);
    }

    /**
     * The method invokes an EntryProcessor for a given key for a valid user 
     * thread.
     * @param oKey Object key to run the EntryProcessor to run against
     * @param agent EntryProcessor
     * @return Object The result of the EntryProcessor
     * @since 04/05/2008
     */
    public Object invoke(Object oKey, InvocableMap.EntryProcessor agent) {
        checkAccess(false);
        return super.invoke(oKey, agent);
    }

    /**
     * The method invokes an EntryProcessor against a Collection of cache keys
     * for a valid user thread.
     * @param collKeys Collection of cache keys to run an EntryProcessor on
     * @param agent EntryProcessor
     * @return Map of values
     * @since 04/05/2008
     */
    public Map invokeAll(Collection collKeys, EntryProcessor agent) {
        checkAccess(false);
        return super.invokeAll(collKeys, agent);
    }

    /**
     * The method runs an EntryProcessor against a key satisfying the passed
     * Filter
     * @param filter Filter
     * @param agent EntryProcessor
     * @return Map of results from the EntryProcessor for each cache key that
     * it was run against
     * @since 04/05/2008
     */
    public Map invokeAll(Filter filter, EntryProcessor agent) {
        checkAccess(false);
        return super.invokeAll(filter, agent);
    }

    /**
     * The method runs an Aggregator for a given Collection of cache keys for 
     * a valid user thread.
     * @param collKeys Collection
     * @param agent EntryAggregator
     * @return Object The result of the Aggregator
     * @since 04/05/2008
     */
    public Object aggregate(Collection collKeys, EntryAggregator agent) {
        checkAccess(true);
        return super.aggregate(collKeys, agent);
    }

    /**
     * The method runs an Aggregator for a set of keys satisfying the passed 
     * Filter for a valid user thread.
     * @param filter Filter
     * @param agent EntryAggregator
     * @return Object Result of the Aggregator
     * @since 04/05/2008
     */
    public Object aggregate(Filter filter, EntryAggregator agent) {
        checkAccess(true);
        return super.aggregate(filter, agent);
    }

    /**
     * The method returns the Subject passed from the user request thread to 
     * the EntitledNamedCache.
     * @return Subject The user subject passed or Null if SecurityManager can
     * not retrieve one
     * @since 04/05/2008
     */
    public static Subject getCurrentSubject() {
        SecurityManager manager = System.getSecurityManager();
        Object oContext = (manager == null) ? AccessController.getContext() : manager.getSecurityContext();
        if (oContext instanceof AccessControlContext) {
            DomainCombiner dC = ((AccessControlContext) oContext).getDomainCombiner();
            if (dC instanceof SubjectDomainCombiner) {
                return ((SubjectDomainCombiner) dC).getSubject();
            }
        }
        return null;
    }

    /**
     * The method checks if the subject passed to the EntitledNamedCache has a
     * valid Principal assigned.
     * @since 04/05/2008
     */
    private void checkAccess(boolean readOnly) {
        logger.debug("Checking access readOnly="+ readOnly + " subject=" + CoherenceSecurityUtils.getCurrentSubject() + " cacheName=" + cacheName);
        if (!provider.checkAccess(getCurrentSubject(), cacheName, readOnly)) {
            throw new SecurityException("Access denied, Insufficient privileges : " + cacheName + " : " + readOnly + " subject=" + CoherenceSecurityUtils.getCurrentSubject());
        }
    }

}
