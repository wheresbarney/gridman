package org.gridman.coherence.backup;

import com.tangosol.net.cache.CacheStore;

import java.util.Collection;
import java.util.Map;

/**
 * @author Jonathan Knight
 */
public class CacheStoreStub implements CacheStore {

    @Override
    public void store(Object key, Object value) {
        System.err.println("store key=" + key + " value=" + value);
    }

    @Override
    public void storeAll(Map map) {
        System.err.println("storeAll map=" + map);
    }

    @Override
    public void erase(Object key) {
        System.err.println("erase key=" + key);
    }

    @Override
    public void eraseAll(Collection keys) {
        System.err.println("eraseAll keys=" + keys);
    }

    @Override
    public Object load(Object key) {
        System.err.println("load " + key);
        return null;
    }

    @Override
    public Map loadAll(Collection keys) {
        System.err.println("loadAll keys=" + keys);
        return null;
    }
}
