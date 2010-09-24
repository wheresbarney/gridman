package org.gridman.coherence.security.simple;

import com.tangosol.net.CacheService;
import com.tangosol.net.WrapperCacheService;
import com.tangosol.net.NamedCache;
import org.apache.log4j.Logger;

/**
 * This provides read/write access to particular caches.
 */
public class SimpleCacheServiceProxy extends WrapperCacheService {

    private static final Logger logger = Logger.getLogger(SimpleCacheServiceProxy.class);

    private CacheSecurityProvider securityProvider;

    public SimpleCacheServiceProxy(CacheService cacheService, String securityProviderName) throws Throwable {
        super(cacheService);
        logger.debug(SimpleCacheServiceProxy.class.getName());
        securityProvider = (CacheSecurityProvider)Class.forName(securityProviderName).newInstance();
    }

    @Override public NamedCache ensureCache(String cache, ClassLoader classLoader) {
        logger.debug("ensureCache : " + cache + " : " + CoherenceUtils.getCurrentSubject());
        return new PermissionedNamedCache(super.ensureCache(cache, classLoader), securityProvider);
    }
}
