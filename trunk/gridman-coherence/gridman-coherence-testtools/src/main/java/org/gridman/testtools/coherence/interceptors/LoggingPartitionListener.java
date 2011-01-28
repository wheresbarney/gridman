package org.gridman.testtools.coherence.interceptors;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.partition.PartitionEvent;
import com.tangosol.net.partition.PartitionListener;

/**
 * @author Jonathan Knight
 */
public class LoggingPartitionListener implements PartitionListener {

    public LoggingPartitionListener() {
        CacheFactory.log("********** Creating Partition Listener");
    }

    @Override
    public void onPartitionEvent(final PartitionEvent partitionEvent) {
        int eventId = partitionEvent.getId();
        String eventName;
        switch (eventId) {
            case PartitionEvent.PARTITION_ASSIGNED:
                eventName = "PARTITION_ASSIGNED";
                log(partitionEvent, eventName);
                break;
            case PartitionEvent.PARTITION_LOST:
                eventName = "******** PARTITION_LOST ********* ";
                log(partitionEvent, eventName);
                break;
            case PartitionEvent.PARTITION_RECEIVE_BEGIN:
                eventName = "PARTITION_RECEIVE_BEGIN";
                log(partitionEvent, eventName);
                break;
            case PartitionEvent.PARTITION_RECEIVE_COMMIT:
                eventName = "PARTITION_RECEIVE_COMMIT";
                log(partitionEvent, eventName);
                break;
            case PartitionEvent.PARTITION_TRANSMIT_BEGIN:
                eventName = "PARTITION_TRANSMIT_BEGIN";
                log(partitionEvent, eventName);
                break;
            case PartitionEvent.PARTITION_TRANSMIT_COMMIT:
                eventName = "PARTITION_TRANSMIT_COMMIT";
                log(partitionEvent, eventName);
                break;
            case PartitionEvent.PARTITION_TRANSMIT_ROLLBACK:
                eventName = "PARTITION_TRANSMIT_ROLLBACK";
                log(partitionEvent, eventName);
                break;
            default:
                CacheFactory.log("Unknown PartitionEvent type=" + eventId, CacheFactory.LOG_DEBUG);
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
}
