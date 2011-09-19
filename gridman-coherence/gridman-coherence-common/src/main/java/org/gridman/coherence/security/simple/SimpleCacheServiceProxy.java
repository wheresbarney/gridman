package org.gridman.coherence.security.simple;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.WrapperCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provides read/write access to particular caches.
 */
public class SimpleCacheServiceProxy extends WrapperCacheService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCacheServiceProxy.class);

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
        logger.debug("ensureCache : " + cache + " : " + CoherenceSecurityUtils.getCurrentSubject());
        return new SimplePermissionedNamedCache(super.ensureCache(cache, classLoader), securityProvider);
    }
}
