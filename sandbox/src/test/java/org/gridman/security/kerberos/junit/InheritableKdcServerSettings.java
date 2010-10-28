package org.gridman.security.kerberos.junit;

import org.apache.directory.server.core.integ.Level;
import org.apache.directory.server.core.integ.SetupMode;
import org.apache.directory.server.core.integ.annotations.*;
import org.gridman.security.kerberos.junit.apacheds.ApacheDsServerContext;
import org.junit.runner.Description;

import java.util.*;


/**
 * Inheritable settings of a test suite, test class, or test method.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class InheritableKdcServerSettings {
    /**
     * the default setup mode to use if inheritance leads to null value
     */
    public static final SetupMode DEFAULT_MODE = SetupMode.ROLLBACK;

    /** The default suffix to use for the LDAP partition if not specified in the LoadLdifFiles and LoadLdifs annotations */
    private String[] DEFAULT_PARTITON_SUFFIX = {"dc=gridman,dc=com"};

    /**
     * the default factory to use if inheritance leads to a null value
     */
    public static final ServerFactory DEFAULT_FACTORY = ServerFactory.DEFAULT;

    public static final ServerPartitionFactory DEFAULT_PARTITION_FACTORY = ServerPartitionFactory.DEFAULT;

    /**
     * parent settings to inherit from
     */
    private final InheritableKdcServerSettings parent;

    /**
     * JUnit test description containing all annotations queried
     */
    private final Description description;

    /**
     * default level at which a service is cleaned up
     */
    private static final Level DEFAULT_CLEANUP_LEVEL = Level.SUITE;


    /**
     * Creates a new InheritableServerSettings instance for test suites description.
     *
     * @param description JUnit description for the suite
     */
    public InheritableKdcServerSettings(Description description) {
        this.description = description;
        this.parent = null;
    }


    /**
     * Creates a new InheritableServerSettings instance based on a test object's
     * description and it's parent's settings.
     *
     * @param description JUnit description for the test object
     * @param parent      the parent settings or null if the test entity is a suite
     */
    public InheritableKdcServerSettings(Description description, InheritableKdcServerSettings parent) {
        this.description = description;
        this.parent = parent;

        if (description.isSuite() && !isSuiteLevel()) {
            throw new IllegalStateException(String.format("The parent must be null for %s suite",
                    description.getDisplayName()));
        }
    }


    /**
     * @return the description of the running test
     */
    public Description getDescription() {
        return description;
    }


    /**
     * @return the settings inherited from the parent
     */
    public InheritableKdcServerSettings getParent() {
        return parent;
    }


    /**
     * @return <code>true</code> if we are at the suite level
     */
    public boolean isSuiteLevel() {
        return parent == null;
    }


    /**
     * @return <code>true</code> if we are at the class level
     */
    public boolean isClassLevel() {
        return (parent != null) && (parent.getParent() == null);
    }


    /**
     * @return <code>true</code> if we are at the method level
     */
    public boolean isMethodLevel() {
        return (parent != null) && (parent.getParent() != null);
    }


    /**
     * @return the test mode. Default to ROLLBACK
     */
    public SetupMode getMode() {
        SetupMode parentMode = DEFAULT_MODE;

        if (parent != null) {
            parentMode = parent.getMode();
        }

        // Get the @Mode annotation
        Mode annotation = description.getAnnotation(Mode.class);

        if (annotation == null) {
            return parentMode;
        } else {
            return annotation.value();
        }
    }

    /**
     * @return the type of the KDC being used for these tests
     */
    public Class<? extends KdcServerContext> getKdcContextClass() {
        Class<? extends KdcServerContext> type = ApacheDsServerContext.class;

        // Get the @KdcType annotation
        KdcType annotation = description.getAnnotation(KdcType.class);
        if (annotation == null) {
            if (parent != null) {
                type = parent.getKdcContextClass();
            }
        } else {
            type = annotation.type();
        }

        return type;
    }

    /**
     * @return the DirectoryService factory
     * @throws IllegalAccessException if we can't access the factory
     * @throws InstantiationException if the DirectoryService can't be instanciated
     */
    public ServerFactory getFactory() throws IllegalAccessException, InstantiationException {
        ServerFactory parentFactory = DEFAULT_FACTORY;

        if (parent != null) {
            parentFactory = parent.getFactory();
        }

        Factory annotation = description.getAnnotation(Factory.class);

        if (annotation == null) {
            return parentFactory;
        } else {
            return (ServerFactory) annotation.value().newInstance();
        }
    }

    public ServerPartitionFactory getPartitionFactory() throws IllegalAccessException, InstantiationException {
        ServerPartitionFactory parentFactory = DEFAULT_PARTITION_FACTORY;

        if (parent != null) {
            parentFactory = parent.getPartitionFactory();
        }

        PartitionFactory annotation = description.getAnnotation(PartitionFactory.class);

        if (annotation == null) {
            return parentFactory;
        } else {
            return (ServerPartitionFactory) annotation.value().newInstance();
        }
    }

    public String getKeyTabFile() {
        String filename = null;
        Krb5 annotation = description.getAnnotation(Krb5.class);
        if (annotation != null) {
            filename = annotation.keytabFile();
        } else if (parent != null) {
            filename = parent.getKeyTabFile();
        }
        return filename;
    }

    public Map<Krb5.FIELDS,Object> getKrb5Settings() {
        Map<Krb5.FIELDS,Object> krb5Settings = new HashMap<Krb5.FIELDS,Object>();

        Krb5 annotation = description.getAnnotation(Krb5.class);
        if (annotation != null) {
            krb5Settings.put(Krb5.FIELDS.CREDENTIALS, annotation.credentials());
            krb5Settings.put(Krb5.FIELDS.KDCHOST, annotation.host());
            krb5Settings.put(Krb5.FIELDS.KDCPORT, annotation.kdcPort());
            krb5Settings.put(Krb5.FIELDS.KEYTAB_FILENAME, annotation.keytabFile());
            krb5Settings.put(Krb5.FIELDS.KRB5CONF, annotation.krb5Conf());
            krb5Settings.put(Krb5.FIELDS.REALM, annotation.realm());
        } else if (parent != null) {
            krb5Settings = parent.getKrb5Settings();
        }

        return krb5Settings;
    }

    public int getKdcPort() {
        int port = Krb5.DEFAULT_PORT;
        Krb5 annotation = description.getAnnotation(Krb5.class);
        if (annotation != null) {
            port = annotation.kdcPort();
        } else if (parent != null) {
            port = parent.getKdcPort();
        }
        return port;
    }
        
    public Map<JAAS.FIELDS,Object> getJaasSettings() {
        Map<JAAS.FIELDS,Object> jaas5Settings = new HashMap<JAAS.FIELDS,Object>();

        JAAS annotation = description.getAnnotation(JAAS.class);
        if (annotation != null) {
            jaas5Settings.put(JAAS.FIELDS.FILENAME, annotation.fileName());
            jaas5Settings.put(JAAS.FIELDS.MODULENAME, annotation.moduleName());
            jaas5Settings.put(JAAS.FIELDS.MODULECLASS, annotation.loginModuleClass());
            jaas5Settings.put(JAAS.FIELDS.SETTINGS, annotation.settings());
        } else if (parent != null) {
            jaas5Settings = parent.getJaasSettings();
        }

        return jaas5Settings;
    }

    public String[][] getKtabArgs() {
        String[][] args = null;

        Krb5 annotation = description.getAnnotation(Krb5.class);
        if (annotation != null) {
            String[] credentials = annotation.credentials();
            args = new String[credentials.length][5];
            for(int i=0; i<credentials.length; i++) {
                args[i][0] = "-a";
                String[] parts = credentials[i].split(",");
                args[i][1] = parts[0];
                args[i][2] = parts[1];
                args[i][3] = "-k";
                args[i][4] = annotation.keytabFile();
            }
        } else if (parent != null){
            args = parent.getKtabArgs();
        }
        return args;
    }

    public String[] getDirectoryPartitionSuffix() {
        String[] suffix = DEFAULT_PARTITON_SUFFIX;

        if (parent != null) {
            suffix = parent.getDirectoryPartitionSuffix();
        }

        DirectoryPartition annotation = description.getAnnotation(DirectoryPartition.class);
        return annotation != null ? annotation.suffixes() : suffix;
    }

    /**
     * Get a list of entries from a LDIF declared as an annotation
     *
     * @param ldifs the list of LDIFs we want to feed
     * @return a list of entries described using a LDIF format
     */
    public List<String> getLdifs(List<String> ldifs) {
        if (ldifs == null) {
            ldifs = new ArrayList<String>();
        }

        if (parent != null) {
            parent.getLdifs(ldifs);
        }

        ApplyLdifs annotation = description.getAnnotation(ApplyLdifs.class);

        if ((annotation != null) && (annotation.value() != null)) {
            ldifs.addAll(Arrays.asList(annotation.value()));
        }

        return ldifs;
    }

    /**
     * Get a list of files containing entries described using the LDIF format.
     *
     * @param ldifFiles the list to feed
     * @return a list of files containing some LDIF data
     */
    public List<String> getLdifFiles(List<String> ldifFiles) {
        if (ldifFiles == null) {
            ldifFiles = new ArrayList<String>();
        }

        if (parent != null) {
            parent.getLdifFiles(ldifFiles);
        }

        ApplyLdifFiles annotation = description.getAnnotation(ApplyLdifFiles.class);

        if ((annotation != null) && (annotation.value() != null)) {
            ldifFiles.addAll(Arrays.asList(annotation.value()));
        }

        return ldifFiles;
    }


    /**
     * @return teh cleanup level. Defualt to SUITE
     */
    public Level getCleanupLevel() {
        Level parentCleanupLevel = DEFAULT_CLEANUP_LEVEL;

        if (parent != null) {
            parentCleanupLevel = parent.getCleanupLevel();
        }

        CleanupLevel annotation = description.getAnnotation(CleanupLevel.class);

        if (annotation == null) {
            return parentCleanupLevel;
        } else {
            return annotation.value();
        }
    }
}