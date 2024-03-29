package org.gridman.testtools.classloader;

import org.gridman.testtools.PropertyDumper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ClassloaderRunnerTest {
    private Properties savedProperties;
    private ClassLoader savedClassLoader;

    @Before
    public void save() {
        savedProperties = System.getProperties();
        savedClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @After
    public void restore() {
        System.setProperties(savedProperties);
        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Test
    public void shouldRunWithIsolatedProperties() throws Throwable {
        Properties originalSystemProperties = new Properties();
        originalSystemProperties.putAll(System.getProperties());

        Properties runnerOneProperties = new Properties();
        runnerOneProperties.setProperty(PropertyDumper.FILE_PROPERTY, "runnerOne.txt");
        runnerOneProperties.setProperty("test", "runnerOne");
        runnerOneProperties.setProperty("key-1", "value-1");

        runSandbox(runnerOneProperties);

        Properties runnerTwoProperties = new Properties();
        runnerTwoProperties.setProperty(PropertyDumper.FILE_PROPERTY, "runnerTwo.txt");
        runnerTwoProperties.setProperty("test", "runnerTwo");
        runnerTwoProperties.setProperty("key-2", "value-2");

        runSandbox(runnerTwoProperties);

        assertEquals(originalSystemProperties, System.getProperties());
        assertProperties(runnerOneProperties, originalSystemProperties);
        assertProperties(runnerTwoProperties, originalSystemProperties);
    }

    public void assertProperties(Properties threadProperties, Properties systemProperties) throws Exception {
        StringBuilder expected = new StringBuilder();
        StringBuilder actual = new StringBuilder();

        Properties properties = new Properties();
        properties.putAll(threadProperties);
        properties.putAll(systemProperties);

        ArrayList<String> keySet = new ArrayList<String>(properties.stringPropertyNames());
        Collections.sort(keySet);
        for (String key : keySet) {
            if (!Arrays.asList(PropertyDumper.ignored).contains(key)) {
                expected.append(key).append("=").append(properties.getProperty(key));
            }
        }

        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = threadProperties.getProperty(PropertyDumper.FILE_PROPERTY);

        File file = new File(tempDir, filename);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null) {
            actual.append(line);
            line = reader.readLine();
        }

        assertEquals(expected.toString(), actual.toString());
    }

    public void runSandbox(final Properties properties) throws Throwable {
        ClassloaderRunner runner = new ClassloaderRunner(PropertyDumper.class.getCanonicalName(), properties);
        while (!runner.isStarted()) {
            Thread.sleep(100);
        }
    }
}
