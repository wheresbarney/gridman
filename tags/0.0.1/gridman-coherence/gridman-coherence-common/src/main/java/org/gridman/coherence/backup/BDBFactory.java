package org.gridman.coherence.backup;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.tangosol.io.bdb.BerkeleyDBBinaryStoreManager;
import com.tangosol.io.bdb.DatabaseFactory;

import java.io.File;

/**
 * @author Jonathan Knight
 */
public class BDBFactory extends DatabaseFactory {

    public BDBFactory(BerkeleyDBBinaryStoreManager bdbManager) throws DatabaseException {
        super(bdbManager);
    }

    @Override
    protected EnvironmentHolder instantiateEnvironment(BerkeleyDBBinaryStoreManager bdbManager) throws DatabaseException {
        return new EnvHolder(bdbManager);
    }

    protected static class EnvHolder extends EnvironmentHolder {
        public EnvHolder(BerkeleyDBBinaryStoreManager bdbManager) throws DatabaseException {
            super(bdbManager);
        }

        @Override
        protected void configure(BerkeleyDBBinaryStoreManager bdbManager) {
            super.configure(bdbManager);
            this.m_envConfig.setLocking(false);
        }

        @Override
        protected void createPersistentEnvironment() throws DatabaseException {
            File dirEnv = this.m_dirEnv = this.m_dirParent;

            if ((!dirEnv.exists()) && (!dirEnv.mkdirs())) {
                throw new IllegalStateException("Unable to create Environment directory " + dirEnv);
            }


            DirectoryLock dirLock = new DirectoryLock(dirEnv, "Locked Coherence persistent Berkeley DB directory.");
            //try {
                //if (dirLock.tryLock()) {
                    File jeLock = new File(dirEnv, "je.lck");
                    if (jeLock.exists()) {
                        jeLock.delete();
                    }
                    this.m_env = new Environment(dirEnv, this.m_envConfig);
                    this.m_dirEnv = dirEnv;
                    this.m_dirLock = dirLock;
                //} else {
                //    throw new UnsupportedOperationException("Unable to open environment " + dirEnv + " already locked.");
                //}
            //}
            //catch (IOException e) {
            //    throw new WrapperException(e, "Error locking directory " + dirEnv);
            //}
        }
    }
}
