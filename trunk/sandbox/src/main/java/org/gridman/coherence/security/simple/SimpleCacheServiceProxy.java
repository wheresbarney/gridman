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
public class SimpleCacheServiceProxy extends WrapperCacheService {

    private static final Logger logger = Logger.getLogger(SimpleCacheServiceProxy.class);

    private SecurityProvider securityProvider;

    // @todo - is this caching a good idea here?
    Map<String, NamedCache> cacheMap = new ConcurrentHashMap();

    public SimpleCacheServiceProxy(CacheService cacheService, String securityProviderName) throws Throwable {
        super(cacheService);
        try {
            logger.debug(SimpleCacheServiceProxy.class.getName());
            securityProvider = (SecurityProvider)Class.forName(securityProviderName).newInstance();
        } catch(Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @Override public NamedCache ensureCache(String cache, ClassLoader classLoader) {
        logger.debug("ensureCache : " + cache + " : " + CoherenceUtils.getCurrentSubject());
        NamedCache cacher = cacheMap.get(cache);
        if(cacher == null) {
            cacher = new PermissionedNamedCache(super.ensureCache(cache, classLoader), securityProvider);
            cacheMap.put(cache,cacher);
        }
        return cacher; 
    }
}
