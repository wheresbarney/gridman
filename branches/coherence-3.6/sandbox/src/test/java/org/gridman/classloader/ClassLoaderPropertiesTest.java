package org.gridman.classloader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.*;

public class ClassLoaderPropertiesTest {

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
    public void shouldUseSystemPropertiesWhenClassLoaderIsNotChildFirstClassLoader() {
        // When we are not using a ChildFirstClassLoader
        assertTrue(!(Thread.currentThread().getContextClassLoader() instanceof ChildFirstClassLoader));

        // And using ClassLoaderProperties
        ClassLoaderProperties.use();

        // Then properties to use should be the System properties
        assertEquals(System.getProperties(), ClassLoaderProperties.getInstance().getPropertiesToUse());
    }

    @Test
    public void shouldUseClassLoaderPropertiesWhenClassLoaderIsChildFirstClassLoader() {
        // When we are using a ChildFirstClassLoader
        ChildFirstClassLoader classLoader = new ChildFirstClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);

        // With these Properties
        Properties classLoaderProperties = new Properties();
        classLoaderProperties.setProperty("Test", "Test-value");
        classLoader.setProperties(classLoaderProperties);

        // And using ClassLoaderProperties
        ClassLoaderProperties.use();
        
        // Then properties to use should equal class loader properties
        assertEquals(classLoaderProperties, ClassLoaderProperties.getInstance().getPropertiesToUse());
    }

    @Test
    public void shouldHaveClassLoaderIsolatedProperties() {
        // When using ClassLoader Properties
        System.setProperties(ClassLoaderProperties.getInstance());

        // With ClassLoader One
        ChildFirstClassLoader classLoaderOne = new ChildFirstClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
        // use ClassLoader One
        Thread.currentThread().setContextClassLoader(classLoaderOne);
        // setting property specific to ClassLoader One
        System.setProperty("test-1", "value-1");

        // And ClassLoader Two
        ChildFirstClassLoader classLoaderTwo = new ChildFirstClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
        // use ClassLoader One
        Thread.currentThread().setContextClassLoader(classLoaderTwo);
        // Setting property specific to ClassLoader Two
        System.setProperty("test-2", "value-2");

        // reset original ClassLoader 
        Thread.currentThread().setContextClassLoader(savedClassLoader);

        // System Properties should not contain test-1 or test-2
        assertFalse(System.getProperties().containsKey("test-1"));
        assertFalse(System.getProperties().containsKey("test-2"));

        // ClassLoader One Properties should contain test-1 and not test-2
        assertTrue(classLoaderOne.getProperties().containsKey("test-1"));
        assertFalse(classLoaderOne.getProperties().containsKey("test-2"));

        // ClassLoader Two Properties should contain test-2 and not test-1
        assertFalse(classLoaderTwo.getProperties().containsKey("test-1"));
        assertTrue(classLoaderTwo.getProperties().containsKey("test-2"));
    }
}
