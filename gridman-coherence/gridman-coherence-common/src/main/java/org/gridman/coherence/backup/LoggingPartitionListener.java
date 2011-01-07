package org.gridman.coherence.backup;

import com.tangosol.io.Serializer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.PartitionedService;
import com.tangosol.net.partition.PartitionEvent;
import com.tangosol.net.partition.PartitionListener;
import com.tangosol.net.partition.PartitionSet;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Jonathan Knight
 */
public class LoggingPartitionListener implements PartitionListener {

    private ExecutorService pool;

    public LoggingPartitionListener() {
        pool = Executors.newFixedThreadPool(10);
    }

    @Override
    public void onPartitionEvent(final PartitionEvent partitionEvent) {
        int eventId = partitionEvent.getId();
        String eventName;
        switch (eventId) {
//            case PartitionEvent.PARTITION_ASSIGNED:
//                eventName = "PARTITION_ASSIGNED";
//                log(partitionEvent, eventName);
//                break;
            case PartitionEvent.PARTITION_LOST:
                eventName = "******** PARTITION_LOST ********* ";
                log(partitionEvent, eventName);
                Future task = pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        restore(partitionEvent.getService(), partitionEvent.getPartitionSet());
                    }
                });

                CacheFactory.log("Waiting for restore task to complete...", CacheFactory.LOG_DEBUG);
//                while(!task.isDone()) {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        // ignored
//                    }
//                }
                CacheFactory.log("Restore task to completed for partitions " + partitionEvent.getPartitionSet(), CacheFactory.LOG_DEBUG);
                
                break;
//            case PartitionEvent.PARTITION_RECEIVE_BEGIN:
//                eventName = "PARTITION_RECEIVE_BEGIN";
//                log(partitionEvent, eventName);
//                break;
//            case PartitionEvent.PARTITION_RECEIVE_COMMIT:
//                eventName = "PARTITION_RECEIVE_COMMIT";
//                log(partitionEvent, eventName);
//                break;
//            case PartitionEvent.PARTITION_TRANSMIT_BEGIN:
//                eventName = "PARTITION_TRANSMIT_BEGIN";
//                log(partitionEvent, eventName);
//                break;
//            case PartitionEvent.PARTITION_TRANSMIT_COMMIT:
//                eventName = "PARTITION_TRANSMIT_COMMIT";
//                log(partitionEvent, eventName);
//                break;
//            case PartitionEvent.PARTITION_TRANSMIT_ROLLBACK:
//                eventName = "PARTITION_TRANSMIT_ROLLBACK";
//                log(partitionEvent, eventName);
//                break;
//            default:
//                CacheFactory.log("Unknown PartitionEvent type=" + eventId, CacheFactory.LOG_DEBUG);
        }
    }

    private void log(PartitionEvent partitionEvent, String eventName) {
        CacheFactory.log("PartitionEvent " + eventName
                + " partitions=" + partitionEvent.getPartitionSet()
                + " toMember=" + partitionEvent.getToMember()
                + " fromMember=" + partitionEvent.getFromMember()
                + " service=" + partitionEvent.getService()
                + " source=" + partitionEvent.getSource()
                , CacheFactory.LOG_DEBUG);
    }

    @SuppressWarnings({"unchecked"})
    private void restore(final PartitionedService partitionedService, final PartitionSet partitions) {
        if (partitionedService instanceof CacheService) {
            final CacheService cacheService = (CacheService)partitionedService;
            final String directory = System.getProperty("backup.bdb.directory");

            List<Future> cacheTasks = new ArrayList<Future>();

            Enumeration<String> cacheNames = cacheService.getCacheNames();
            while (cacheNames.hasMoreElements()) {
                final String cacheName = cacheNames.nextElement();
                CacheFactory.log("Data Loss " + cacheName, CacheFactory.LOG_DEBUG);

                Future task = pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        List<Future> partitionTasks = new ArrayList<Future>();
                        final NamedCache cache = CacheFactory.getCache(cacheName);
                        final Serializer serializer = cacheService.getSerializer();
                        for (final int partition : partitions.toArray()) {
                            Future task = pool.submit(new Runnable() {
                                @Override
                                public void run() {
                                    CacheFactory.log("Recovering " + cacheName + " partition=" + partition);
                                    BDBStoreManager manager = new BDBStoreManager(new File(directory), cacheName + "$Backup-" + partition);
                                    BDBStore store = (BDBStore) manager.createBinaryStore();

                                    Iterator<Binary> keys = store.keys();
                                    while (keys.hasNext()) {
                                        Binary key = keys.next();
                                        Binary value = store.load(key);

                                        Object oKey = ExternalizableHelper.fromBinary(key, serializer);
                                        Object oValue = ExternalizableHelper.fromBinary(value, serializer);
                                        CacheFactory.log("Recovering " + cacheName + " partition=" + partition + "key=" + oKey + " value=" + oValue);
                                        cache.put(oKey, oValue);
                                    }

                                    store.close();
                                }
                            });
                            partitionTasks.add(task);
                        }

                        waitForTasks(partitionTasks);
                    }
                });

                cacheTasks.add(task);
            }

            waitForTasks(cacheTasks);
        }

    }

    private void waitForTasks(List<Future> tasks) {
        boolean done = false;
        while(!done) {
            for (Future task : tasks) {
                if (!task.isDone()) {
                    done = false;
                    continue;
                }
                done = true;
            }
        }
    }
}
