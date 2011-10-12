package org.gridman.security.kerberos.junit;

import java.io.File;
import java.util.UUID;

/**
 * @author Jonathan Knight
 */
public class KerberosUtils {

    public static File createTempDirectory() {
        File dir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        dir.mkdirs();
        return dir;
    }

    public static void deleteTempDirectory(File directory) {
        if(directory.isDirectory()) {
            for (File child : directory.listFiles()) {
                deleteTempDirectory(child);
            }
        }
        directory.delete();
    }
    
    public static void generateKeytab(String principal, String password, String filename) {
        Ktab.main(new String[]{"-a", principal, password, "-k", filename});
    }

}
