package org.gridman.coherence.security.cluster;

import com.tangosol.net.ClusterPermission;

import javax.security.auth.Subject;

/**
 * An implementation of a com.tangosol.net.security.AccessController
 * that does no checking and allows all access.<p/>
 *
 * @author jonathan knight
 */
public class AllowAllAccessController extends BaseClusterAccessController {

    public AllowAllAccessController(String keyStoreName, String alias, String password) {
        super(keyStoreName, alias, password);
    }

    /**
     * Overrides the super class method to do nothing.<p/>
     * This will allow all access checks to pass.<p/>
     *
     * @param clusterPermission - the requested permission
     * @param subject           - the Subject object representing the requestor
     */
    public void checkPermission(ClusterPermission clusterPermission, Subject subject) {
    }
}
