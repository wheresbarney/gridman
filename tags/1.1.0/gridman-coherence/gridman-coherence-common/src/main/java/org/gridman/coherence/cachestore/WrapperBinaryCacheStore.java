package org.gridman.coherence.cachestore;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.cache.BinaryEntryStore;
import com.tangosol.net.cache.CacheStore;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;

import java.util.Set;

public class WrapperBinaryCacheStore implements BinaryEntryStore {

    private BackingMapManagerContext context;

    private CacheStore wrapped;

    public WrapperBinaryCacheStore(BackingMapManagerContext context, ClassLoader loader, String cacheName, XmlElement cacheStoreConfig) {
        this.context = context;
        DefaultConfigurableCacheFactory cacheFactory = (DefaultConfigurableCacheFactory) CacheFactory.getConfigurableCacheFactory();
        DefaultConfigurableCacheFactory.CacheInfo info = cacheFactory.findSchemeMapping(cacheName);
        XmlElement xmlConfig = cacheStoreConfig.getSafeElement("class-scheme");
        wrapped = (CacheStore)cacheFactory.instantiateAny(info, xmlConfig, context, loader);
    }

    @Override
    public void erase(BinaryEntry binaryEntry) {
        wrapped.erase(binaryEntry.getKey());
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void eraseAll(Set entries) {
        for (BinaryEntry entry : (Set<BinaryEntry>)entries) {
            erase(entry);
        }
    }

    @Override
    public void load(BinaryEntry binaryEntry) {
        Object value = wrapped.load(binaryEntry.getKey());
        binaryEntry.updateBinaryValue((Binary) context.getValueToInternalConverter().convert(value));
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void loadAll(Set entries) {
        for (BinaryEntry entry : (Set<BinaryEntry>)entries) {
            load(entry);
        }
    }

    @Override
    public void store(BinaryEntry binaryEntry) {
        wrapped.store(binaryEntry.getKey(), binaryEntry.getValue());
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void storeAll(Set entries) {
        for (BinaryEntry entry : (Set<BinaryEntry>)entries) {
            store(entry);
        }
    }

}
