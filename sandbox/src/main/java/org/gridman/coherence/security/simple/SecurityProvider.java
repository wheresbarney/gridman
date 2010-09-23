package org.gridman.coherence.security.simple;

import javax.security.auth.Subject;

public interface SecurityProvider {
    public boolean checkAccess (Subject subject, boolean readOnly, String cacheName);
}
