package org.gridman.coherence.security.simple;

import com.tangosol.net.CacheService;
import com.tangosol.net.WrapperCacheService;
import com.tangosol.net.NamedCache;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This provides read/write access to particular caches.
 */
public class CheckCacheServiceProxy extends WrapperCacheService {

    private static final Logger logger = Logger.getLogger(CheckCacheServiceProxy.class);

    private SecurityProvider sp = new CheckSecurityProvider();

    Map<String, NamedCache> cacheMap = new ConcurrentHashMap();

    public CheckCacheServiceProxy(CacheService cacheService) {
        super(cacheService);
        logger.debug(CheckCacheServiceProxy.class.getName());
    }

    @Override public NamedCache ensureCache(String cache, ClassLoader classLoader) {
        logger.debug("ensureCache : " + cache + " : " + CoherenceUtils.getCurrentSubject());
        NamedCache cacher = cacheMap.get(cache);
        if(cacher == null) {
            cacher = new PermissionedNamedCache(super.ensureCache(cache, classLoader),sp);
            cacheMap.put(cache,cacher);
        }
        return cacher; 
    }
}
