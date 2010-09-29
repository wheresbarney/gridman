package org.gridman.kerberos.junit;

import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jonathan Knight
 */
public interface ServerPartitionFactory {

    List<Partition> getPartitions();

    ServerPartitionFactory DEFAULT = new ServerPartitionFactory() {
        @SuppressWarnings({"unchecked"})
        public List<Partition> getPartitions() {
            JdbmPartition partition = new JdbmPartition();
            partition.setId("examples");
            partition.setSuffix("dc=example,dc=com");
            partition.setCacheSize(1000);

            // Create some indices (optional)
            Set indexedAttrs = new HashSet();
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("1.3.6.1.4.1.18060.0.4.1.2.1"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("1.3.6.1.4.1.18060.0.4.1.2.2"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("1.3.6.1.4.1.18060.0.4.1.2.3"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("1.3.6.1.4.1.18060.0.4.1.2.4"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("1.3.6.1.4.1.18060.0.4.1.2.5"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("1.3.6.1.4.1.18060.0.4.1.2.6"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("1.3.6.1.4.1.18060.0.4.1.2.7"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("dc"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("ou"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("krb5PrincipalName"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("uid"));
            indexedAttrs.add(new JdbmIndex<Object, ServerEntry>("objectClass"));
            partition.setIndexedAttributes(indexedAttrs);
            return Collections.singletonList((Partition) partition);
        }
    };
}
