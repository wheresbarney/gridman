package org.gridman.coherence.security.simple;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import org.apache.log4j.Logger;

import java.net.InetAddress;

/**
 * This authorizes cluster membership using an Authorized Host Cache.
 * It is up to the developer to secure access to the AuthorizedHostCache (probably using EntitledNamedCache)
 * Hosts are initially loaded using a System Property, but the Cache can be updated dynamically at runtime
 * (the System Property should also be changed at the same time in case of cluster restart).
 * @author Andrew Wilson 6Apr10
 */
public class AuthorizedHostFilter implements Filter {
    private static final Logger logger = Logger.getLogger(AuthorizedHostFilter.class);

    public AuthorizedHostFilter() {
        logger.debug("AuthorizedHostFilter()");
    }

    public boolean evaluate(Object o)
    {
        // If we're not on the cluster, let them past for now - they'll be rejected by the Senior member.
        if(!CacheFactory.getCluster().isRunning()) { return true; }

        String hostCache = System.getProperty("coherence.authorizedHost.cache","AUTHORIZED_HOST_CACHE");
        NamedCache cache = CacheFactory.getCache(hostCache);

        if(cache.size()==0) {
            // need to prepopulate from system property
            String[] strings = System.getProperty("coherence.authorizedHost.list").split(",");
            for(String host : strings) {
                cache.put(host,host);
            }
        }

        // This cache may have been updated with extra hosts by an admin user.
        String hostname = ((InetAddress)o).getHostName();
        boolean allowed = cache.containsKey(hostname);
        if(!allowed) { logger.error("Rejecting join request from hostname : " + hostname); }
        return allowed;
    }


}
