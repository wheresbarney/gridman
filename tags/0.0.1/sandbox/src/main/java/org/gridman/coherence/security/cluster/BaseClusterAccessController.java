package org.gridman.coherence.security.cluster;

import com.tangosol.net.ClusterPermission;
import com.tangosol.net.security.AccessController;
import com.tangosol.util.Base;

import javax.security.auth.Subject;
import java.io.*;
import java.security.*;

/**
 * This is an abstract implementation of <code>com.tangosol.net.security.AccessController</code>
 * <p/>
 * In order to implement the encrypt and decrypt methods the code needs
 * to work with java.securitySignedObject which requires the use of
 * public and private keys which are not available in Kerberos.
 * To work around this a Java keystore is used
 * which holds a dummy key pair. This keystore is then used by every
 * cache server instance. Coherence uses this to encrypt and decrypt the
 * permission request and also to verify the subject. The use of a common
 * (dummy) keystore is not seen as a risk as the permission request is not
 * sensitive and the Subject is verified by Kerberos in the checkPermission
 * method.
 *
 * @author Jonathan Knight
 */
public abstract class BaseClusterAccessController implements AccessController {

    /**
     * This is the signature algorithm that will be used with our keys to encrypt
     * and decrypt SignedObject instances.
     */
    public static final String SIGNATURE_ALGORITHM = "SHA1withDSA";
    /**
     * The static Signature used for encryption and decryption
     */
    public static final Signature SIGNATURE_ENGINE;

    /**
     * The PrivateKey used for encryption
     */
    private PrivateKey privateKey;

    /**
     * The public key used for decryption
     */
    private PublicKey publicKey;

    // Statically create the Signature Engine

    static {
        try {
            SIGNATURE_ENGINE = Signature.getInstance(SIGNATURE_ALGORITHM);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public BaseClusterAccessController() {
    }

    /**
     * Create a new BaseClusterAccessController using the keys from the specified keystore.
     *
     * @param keyStoreName - the keystore to use
     * @param alias        - the alias whoes keys will be used from the keystore
     * @param password     - the password for the alias
     */
    public BaseClusterAccessController(String keyStoreName, String alias, String password) {
        initKeystore(keyStoreName, alias, password);
    }

    /**
     * Verify that the specified Subject is authorised for the requested permission.
     * <strong>Note:</strong> This method is called very frequently so should be implemented
     * to be as efficient as possible.
     *
     * @param clusterPermission - the requested permission
     * @param subject - the requesting Subject
     */
    @Override
    public abstract void checkPermission(ClusterPermission clusterPermission, Subject subject);

    /**
     * Create a new BaseClusterAccessController using the keys from the specified keystore.
     * <p/>
     * The fileKeyStore must point to a valid Java keystore file. The value can either be the
     * full path and filename or it can point to a file on the classpath.
     * <p/>
     *
     * @param fileKeyStore - the keystore to use
     * @param alias        - the alias whoes keys will be used from the keystore
     * @param password     - the password for the alias
     * @throws IllegalArgumentException if the keyStoreName parameter is null or blank,
     *                                  if the alias parameter is null or blank, if the password parameter is null or blank
     *                                  or if the keysotre file cannot be found
     */
    protected void initKeystore(String fileKeyStore, String alias, String password) {
        try {
            // Verify we have a keystore specified
            if (fileKeyStore == null || fileKeyStore.length() == 0) {
                throw new IllegalArgumentException("KeyStore must be specified");
            }

            // verify we have an alias name to access the keystore
            if (alias == null || alias.length() == 0) {
                throw new IllegalArgumentException("The alias parameter cannot be null or blank");
            }

            // verify we have a password to access the keystore
            if (password == null || password.length() == 0) {
                throw new IllegalArgumentException("The password parameter cannot be null or blank");
            }

            // Extract the keys from the keystore
            InputStream fileStoreStream;

            File f = new File(fileKeyStore);
            if (f.exists()) {
                fileStoreStream = new FileInputStream(f);
            } else {
                fileStoreStream = getClass().getResourceAsStream(fileKeyStore);
            }

            if (fileStoreStream == null) {
                throw new IllegalArgumentException("keystore file [" + fileKeyStore + "] does not exist");
            }

            KeyStore store = KeyStore.getInstance("JKS");
            store.load(fileStoreStream, null);
            privateKey = (PrivateKey) store.getKey(alias, password.toCharArray());
            publicKey = store.getCertificate(alias).getPublicKey();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw Base.ensureRuntimeException(e, "Error in ConfigurableClusterAccessController constructor");
        }
    }


    PrivateKey getPrivateKey() {
        return privateKey;
    }

    PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Encrypt the specified object using the private key extracted from the keystore
     * specified in the constructor for this BaseClusterAccessController.
     *
     * @param o             - the Object to encrypt
     * @param subjEncryptor - the Subject object whose credentials are being used to do the encryption
     * @return the SignedObject
     * @throws java.io.IOException - if an error occurs during serialization
     * @throws java.security.GeneralSecurityException
     *                             - if the signing fails
     */
    public SignedObject encrypt(Object o, Subject subjEncryptor) throws IOException, GeneralSecurityException {
        return new SignedObject((Serializable) o, privateKey, SIGNATURE_ENGINE);
    }

    /**
     * Decrypt the specified SignedObject using the public credentials extracted from the
     * keystore specified in this BaseClusterAccessController constructor.
     *
     * @param signedObject  - the SignedObject to decrypt
     * @param subjEncryptor - the Subject object whose credentials were used to do the encryption
     * @param subjDecryptor - the Subject object whose credentials might be used to do the decryption; for example, in a request/response model, the decryptor for a response is the encryptor for the original request
     * @return the decrypted Object
     * @throws ClassNotFoundException - if a necessary class cannot be found during deserialization
     * @throws java.io.IOException    - if an error occurs during deserialization
     * @throws java.security.GeneralSecurityException
     *                                - if the verification fails
     */
    public Object decrypt(SignedObject signedObject, Subject subjEncryptor, Subject subjDecryptor) throws ClassNotFoundException, IOException, GeneralSecurityException {
        if (!signedObject.verify(publicKey, SIGNATURE_ENGINE)) {
            throw new SignatureException("Unable to verify SignedObject");
        }
        return signedObject.getObject();
    }

}
