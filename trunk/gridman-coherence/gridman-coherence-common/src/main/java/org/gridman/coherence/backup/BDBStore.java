package org.gridman.coherence.backup;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.tangosol.io.bdb.BerkeleyDBBinaryStore;
import com.tangosol.io.bdb.DatabaseFactory;
import com.tangosol.util.Binary;

/**
 * @author Jonathan Knight
 */
public class BDBStore extends BerkeleyDBBinaryStore {

    public BDBStore(String sDbName, DatabaseFactory dbFactory) throws DatabaseException {
        super(sDbName, dbFactory);
        System.err.println("********* Creating BDBStore for " + sDbName);
    }

    public Database getDB() {
        return getDbHolder().getDb();
    }

    @Override
    public void erase(Binary binKey) {
        try {
            System.err.println("*********** " + getDB().getDatabaseName() + " erase " + binKey);
        } catch (DatabaseException e) {
            // ignore
        }
        super.erase(binKey);
    }

    @Override
    public void eraseAll() {
        try {
            System.err.println("*********** " + getDB().getDatabaseName() + " eraseAll ");
        } catch (DatabaseException e) {
            // ignore
        }
        super.eraseAll();
    }
}
