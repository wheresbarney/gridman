package org.gridman.coherence.backup;

import com.sleepycat.je.DatabaseException;
import com.tangosol.io.BinaryStore;
import com.tangosol.io.bdb.BerkeleyDBBinaryStoreManager;
import com.tangosol.io.bdb.DatabaseFactory;
import com.tangosol.util.WrapperException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jonathan Knight
 */
public class BDBStoreManager extends BerkeleyDBBinaryStoreManager {

    private final Map<BerkeleyDBBinaryStoreManager,DatabaseFactory> m_mapFactories = new HashMap<BerkeleyDBBinaryStoreManager,DatabaseFactory>();
    
    public BDBStoreManager() {
    }

    public BDBStoreManager(File dirParent, String cacheName) {
        String[] parts = cacheName.split("\\$");
        String dirName = dirParent.getAbsolutePath() + File.separator + parts[0] + File.separator + cacheName;
        this.m_dirParent = new File(dirName).getAbsoluteFile();
        this.m_sDbName = cacheName;
        this.m_fTemporary = false;
    }

    public BinaryStore createBinaryStore() {
        try {
            return new BDBStore(this.m_sDbName, ensureFactory(this));
        }
        catch (DatabaseException e) {
            throw new WrapperException(e, "Failed to create a Berkeley DB Binary Store.");
        }
    }

    public DatabaseFactory ensureFactory(BerkeleyDBBinaryStoreManager bdbMgr) throws DatabaseException {
        synchronized (m_mapFactories) {
            DatabaseFactory factory = m_mapFactories.get(bdbMgr);
            if (factory == null) {
                factory = new BDBFactory(bdbMgr);
                m_mapFactories.put(bdbMgr, factory);
            }
            return factory;
        }
    }

}
