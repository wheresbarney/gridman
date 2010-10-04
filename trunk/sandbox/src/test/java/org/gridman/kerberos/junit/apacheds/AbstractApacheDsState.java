package org.gridman.kerberos.junit.apacheds;

import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.DefaultServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.integ.ServerIntegrationUtils;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.gridman.kerberos.junit.AbstractKerberosState;
import org.gridman.kerberos.junit.InheritableKdcServerSettings;
import org.gridman.kerberos.junit.KdcServerContext;
import org.gridman.kerberos.junit.ServerPartitionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


/**
 * The abstract state of a test service, containing the default state
 * transitions
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public abstract class AbstractApacheDsState extends AbstractKerberosState {
    /**
     * The class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractApacheDsState.class);

    /**
     * Creates a new instance of AbstractState.
     *
     * @param context The associated context
     */
    protected AbstractApacheDsState(KdcServerContext context) {
        super(context);
    }

    /**
     * Action where an attempt is made to start up the service.
     *
     * @throws Exception on failures to start the core directory service
     */
    public void startup(InheritableKdcServerSettings settings) throws Exception {
        LOG.debug("calling start()");
        startLdap();
        startKerberos();
        super.startup(settings);
    }

    void startLdap() throws Exception {
        KdcServerContext context = getContext();
        context.getDirectoryService().startup();
        LdapServer ldapServer = context.getLdapServer();
        ldapServer.start();

        LdapContext schema = (LdapContext) ServerIntegrationUtils.getWiredContext(ldapServer).lookup("ou=schema");
        Attribute attribute = new BasicAttribute("m-disabled", "false");
        ModificationItem[] mods = new ModificationItem[]{
                new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute)
        };
        schema.modifyAttributes("cn=krb5kdc", mods);
    }

    void startKerberos() throws Exception {
        getContext().getKdcServer().start();
    }

    /**
     * Inject the Ldifs if any
     *
     * @param service  the instantiated directory service
     * @param settings the settings containing the ldif
     */
    protected void injectLdifs(DirectoryService service, InheritableKdcServerSettings settings) throws Exception {
        List<String> ldifs = new ArrayList<String>();
        List<String> ldifFiles = new ArrayList<String>();

        ServerPartitionFactory partitionFactory = settings.getPartitionFactory();
        String[] suffixes = settings.getDirectoryPartitionSuffix();
        List<Partition> partitions = partitionFactory.getPartitions(suffixes);
        getContext().setPartitions(partitions);
        for (Partition partition : partitions) {
            partition.init(service);
            service.addPartition(partition);
        }

        // First inject the LDIF files if any
        ldifFiles = settings.getLdifFiles(ldifFiles);

        if (ldifFiles.size() != 0) {
            for (String ldifFile : ldifFiles) {
                String className = settings.getParent().getDescription().getDisplayName();

                if (className == null) {
                    String message = "Cannot inject a LDIF file with a null name";
                    LOG.error(message);
                    throw new FileNotFoundException(message);
                }

                Class<?> clazz = null;

                try {
                    clazz = Class.forName(className);
                }
                catch (ClassNotFoundException cnfe) {
                    String message = "Cannot inject a LDIF file for this class : " + className;
                    LOG.error(message);
                    throw new FileNotFoundException(message);
                }

                InputStream in = clazz.getResourceAsStream(ldifFile);

                if (in == null) {
                    String message = "Cannot inject a LDIF for the file " + ldifFile;
                    LOG.error(message);
                    throw new FileNotFoundException(message);
                }

                LdifReader ldifReader = new LdifReader(in);

                for (LdifEntry entry : ldifReader) {
                    service.getAdminSession().add(
                            new DefaultServerEntry(service.getRegistries(), entry.getEntry()));
                    LOG.debug("Successfully injected LDIF enry for test {}: {}", settings.getDescription(), entry);
                }
            }
        }

        ldifs = settings.getLdifs(ldifs);

        if (ldifs.size() != 0) {
            for (String ldif : ldifs) {
                StringReader in = new StringReader(ldif);
                LdifReader ldifReader = new LdifReader(in);

                for (LdifEntry entry : ldifReader) {
                    service.getAdminSession().add(
                            new DefaultServerEntry(service.getRegistries(), entry.getEntry()));
                    LOG.debug("Successfully injected LDIF enry for test {}: {}", settings.getDescription(), entry);
                }
            }
        }
    }
    
}