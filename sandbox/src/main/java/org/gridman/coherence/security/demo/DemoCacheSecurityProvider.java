package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import org.apache.log4j.Logger;
import org.gridman.coherence.security.simple.CoherenceUtils;
import org.gridman.coherence.security.simple.CacheSecurityProvider;

import javax.security.auth.Subject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Our Security Provider, it checks the permission in the permissionCache.
 */
public class DemoCacheSecurityProvider implements CacheSecurityProvider {
    public static final Logger logger = Logger.getLogger(DemoCacheSecurityProvider.class);

    public DemoCacheSecurityProvider() {
        logger.debug(DemoCacheSecurityProvider.class.getName());
    }

    /*
     * We need to use an Invoke Service or we'll get cache loopback.
     * Otherwise, we could make the PermissionCache read-only to everyone, which I don't like.
     */
    @Override public boolean checkAccess(Subject subject, String cacheName, boolean readOnly) {
        logger.debug("checkAccess " + cacheName + " : " + readOnly + " : " + subject);
        String role = CoherenceUtils.getFirstPrincipalName(subject);
        InvocationService service = (InvocationService) CacheFactory.getService(DemoServer.SERVER_INVOKE_SERVICE);
        Set<Member> memberSet = Collections.singleton(CacheFactory.getCluster().getLocalMember());
        Map map = service.query(new DemoCachePermissionInvoke(role, cacheName, true, readOnly), memberSet);
        return (Boolean)map.values().iterator().next();
    }
}
