package org.gridman.coherence.security.simple;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.WrapperCacheService;
import org.apache.log4j.Logger;

/**
 * This provides read/write access to particular caches.
 */
public class BaseSecurityCacheServiceProxy extends WrapperCacheService {

    private static final Logger logger = Logger.getLogger(BaseSecurityCacheServiceProxy.class);

    private BaseSecurityCacheProvider securityProvider;

    public BaseSecurityCacheServiceProxy(CacheService cacheService, BaseSecurityCacheProvider securityProvider) throws Exception {
        super(cacheService);
        logger.debug(BaseSecurityCacheServiceProxy.class.getName());
        this.securityProvider = securityProvider;
    }
    
    public BaseSecurityCacheServiceProxy(CacheService cacheService, String securityProviderName) throws Throwable {
        this(cacheService, (BaseSecurityCacheProvider)Class.forName(securityProviderName).newInstance());
    }

    @Override public NamedCache ensureCache(String cache, ClassLoader classLoader) {
        logger.debug("ensureCache : " + cache + " : " + CoherenceSecurityUtils.getCurrentSubject());
        return new BaseSecurityReadWriteWrapperCache(super.ensureCache(cache, classLoader), securityProvider);
    }
}
