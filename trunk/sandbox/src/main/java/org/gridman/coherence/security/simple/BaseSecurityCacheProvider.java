package org.gridman.coherence.security.simple;

import javax.security.auth.Subject;

public interface BaseSecurityCacheProvider {
    public boolean checkAccess (Subject subject, String cacheName, boolean readOnly);
}
