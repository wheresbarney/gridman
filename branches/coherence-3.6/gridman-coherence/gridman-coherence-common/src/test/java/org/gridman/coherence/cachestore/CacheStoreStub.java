package org.gridman.coherence.cachestore;

import com.tangosol.net.cache.AbstractCacheStore;

public class CacheStoreStub extends AbstractCacheStore {

    private String cacheName;

    public CacheStoreStub(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public Object load(Object key) {
        System.err.println("load called cacheName=" + cacheName + " key=" + key);

        Object value = null;

        String[] parts = String.valueOf(key).split("-");
        int id = Integer.parseInt(parts[1]);
        if (id % 2 == 0) {
            value = "value-" + id;
        }

        return value;
    }

    @Override
    public void store(Object key, Object value) {
        System.err.println("store called cacheName=" + cacheName + " key=" + key + " value=" + value);
    }
}
