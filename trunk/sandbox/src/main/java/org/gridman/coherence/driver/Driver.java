package org.gridman.coherence.driver;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.aggregator.Count;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.EqualsFilter;
import org.apache.log4j.Logger;
import org.gridman.classloader.SystemPropertyLoader;

import java.util.*;

/**
 * This is a driver for doing test results etc.
 * @author Andrew Wilson 15/4/10
 */
public class Driver {
    public static final Logger logger = Logger.getLogger(Driver.class);
    
    public Driver() {}

    public void run(Properties properties) throws Throwable {
        SystemPropertyLoader.loadEnvironment("/coherence/driver/driverClient.properties");
        if(properties != null) { System.getProperties().putAll(properties); }
        logger.info("System Properties : " + System.getProperties());

        Integer cacheNo = Integer.getInteger("org.gridman.coherence.driver.cacheNo");
        Integer batchSize = Integer.getInteger("org.gridman.coherence.driver.batchSize");
        Integer dataSize = Integer.getInteger("org.gridman.coherence.driver.dataSize");
        Integer totalKeySize = Integer.getInteger("org.gridman.coherence.driver.totalKeySize");
        Integer sleepTime = Integer.getInteger("org.gridman.coherence.driver.sleepTime");
        Integer reportBatch = Integer.getInteger("org.gridman.coherence.driver.reportBatch");
        String testType = System.getProperty("org.gridman.coherence.driver.test");
        Integer indexSize = Integer.getInteger("org.gridman.coherence.driver.indexSize");

        Integer serverCount = Integer.getInteger("org.gridman.coherence.driver.serverCount");
        Integer client = Integer.getInteger("org.gridman.coherence.driver.client");
        System.setProperty("org.gridman.coherence.driver.address", System.getProperty("org.gridman.coherence.driver.address." + client%serverCount));
        System.out.println("Address : " + System.getProperty("org.gridman.coherence.driver.address"));
        String cacheName = "dist-testCache" + cacheNo;
        Count COUNT = new Count();

        NamedCache cache = CacheFactory.getCache(cacheName);

        System.out.println("Doing : " + testType);

        if(testType.equals("clear")) {
            cache.clear();
            return;
        }
        
        byte[] data = new byte[dataSize];
        for(int i=0;i<dataSize;i++) {
            data[i] = 'X';
        }

        if(testType.equals("populate")) {
            int warmupBatches = totalKeySize / batchSize;
            logger.info("Doing warmup");
            cache.addIndex(new ReflectionExtractor("getIndexValue"),false,null);

            for(int warmupBatch=0;warmupBatch<warmupBatches;warmupBatch++) {
                Map map = new HashMap();
                for(int i=0;i<batchSize;i++) {
                    int key = (warmupBatch*batchSize) + i;
                    //logger.debug("Put" + key);
                    map.put(key, new DriverObject(key%indexSize, data));
                }
                cache.putAll(map);
                logger.info("PutAll : " + warmupBatch*batchSize);
            }

            logger.info("Done warmup");
            return;
        }

        logger.info("Starting test");

        long start = System.nanoTime();
        int counter = 0;
        while(true) {
            int key = (int)(Math.random() * totalKeySize);
            if("put".equals(testType)) {
                cache.put(key,new DriverObject(key%indexSize, data));
            } else if("get".equals(testType)) {
                cache.get(key);
            } else if("putAll".equals(testType)) {
                Map map = new HashMap();
                for(int i=0;i<batchSize;i++) {
                    int putAllKey = (int)(Math.random() * totalKeySize);
                    map.put(putAllKey,new DriverObject(putAllKey%indexSize, data));
                }
                cache.putAll(map);
            } else if("getAll".equals(testType)) {
                Set set = new HashSet();
                for(int i=0;i<batchSize;i++) {
                    int getAllKey = (int)(Math.random() * totalKeySize);
                    set.add(getAllKey);
                }
                //System.out.println("GetAll size" + set.size());
                cache.getAll(set);
            } else if("index".equals(testType)) {
                // An indexed value aggregate
                cache.aggregate(new EqualsFilter("getIndexValue", (int)(Math.random() * indexSize)), COUNT);
            } else if("noIndex".equals(testType)) {
                // A non-indexed value
                cache.aggregate(new EqualsFilter("getSimpleValue", (int)(Math.random() * indexSize)), COUNT);
            } else if("pofExtract".equals(testType)) {
                // A pof-extractor
                EqualsFilter pofFilter = new EqualsFilter(
                        new PofExtractor(Integer.class, DriverObject.POF_OFFSET_SIMPLE_VALUE), (int)(Math.random() * indexSize));
                cache.aggregate(pofFilter, COUNT);
            } else {
                throw new Exception ("Unknown test type " + testType);
            }
            Thread.sleep(sleepTime);
            counter++;
            if(counter%reportBatch == 0) {
                logger.info(testType + " :" + counter + " rate : " + ( 1000000000L*reportBatch/(System.nanoTime()-start) + "/s"));
                start = System.nanoTime();
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        new Driver().run(null);
    }
}
