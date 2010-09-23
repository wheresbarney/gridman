package org.gridman.coherence.security.demo.security;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.WrapperCacheService;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: andrewwilson
 * Date: Sep 21, 2010
 * Time: 11:10:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleCacheProxy extends WrapperCacheService
{
    private static final Logger logger = Logger.getLogger(SimpleCacheProxy.class);

    public SimpleCacheProxy(CacheService service) {
        super(service);
        logger.debug("Calling SimpleCacheProxy");
    }

    @Override public NamedCache ensureCache(String sName, ClassLoader loader) {
        logger.debug("ensureCache : " + sName);
        return super.ensureCache(sName, loader);
    }


}
