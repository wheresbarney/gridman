package org.gridman.coherence.backup;

import com.tangosol.coherence.component.util.BackingMapManagerContext;
import com.tangosol.run.xml.XmlElement;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jonathan Knight
 */
public class BackupMap extends HashMap {

    private XmlElement config;
    private String cacheName;
    private BackingMapManagerContext context;
    private ClassLoader classloader;

    public BackupMap(int i, float v) {
        super(i, v);
        System.err.println("BackupMap - (int, float) Constructor " + System.identityHashCode(this));
    }

    public BackupMap(int i) {
        super(i);
        System.err.println("BackupMap - (int) Constructor " + System.identityHashCode(this));
    }

    public BackupMap() {
        System.err.println("BackupMap - () Constructor " + System.identityHashCode(this));
    }

    public BackupMap(Map map) {
        super(map);
        System.err.println("BackupMap - (Map) Constructor " + System.identityHashCode(this));
    }

    public BackupMap(String cacheName, BackingMapManagerContext context, ClassLoader classloader) {
    }
}
