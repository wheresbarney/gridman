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

    public SimpleCacheServiceProxy(CacheService cacheService, CacheSecurityProvider securityProvider) throws Exception {
        super(cacheService);
        logger.debug(SimpleCacheServiceProxy.class.getName());
        this.securityProvider = securityProvider;
    }
    
    public SimpleCacheServiceProxy(CacheService cacheService, String securityProviderName) throws Throwable {
        this(cacheService, (CacheSecurityProvider)Class.forName(securityProviderName).newInstance());
    }

    @Override public NamedCache ensureCache(String cache, ClassLoader classLoader) {
        logger.debug("ensureCache : " + cache + " : " + CoherenceUtils.getCurrentSubject());
        return new PermissionedNamedCache(super.ensureCache(cache, classLoader), securityProvider);
    }
}
