package org.gridman.classloader;

import java.io.*;
import java.util.*;

/**
 * @todo this is not currently used, but we'd like to hook it in to give us isolation.
 * @author Jonathan Knight
 */
public class ClassLoaderProperties extends Properties {
    private static ClassLoaderProperties sInstance = new ClassLoaderProperties();

    public static ClassLoaderProperties getInstance() { return sInstance; }

    private Properties systemProperties;

    private ClassLoaderProperties() {
        systemProperties = System.getProperties();
        System.setProperties(this);
    }

    private Properties getPropertiesToUse() {
        Properties props;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl instanceof ChildFirstClassLoader) {
            props = ((ChildFirstClassLoader)cl).getProperties();
        } else {
            props = systemProperties;
        }
        return props;
    }

    @Override
    public Object setProperty(String key, String value) {
        return getPropertiesToUse().setProperty(key, value);
    }

    @Override
    public void load(Reader reader) throws IOException {
        getPropertiesToUse().load(reader);
    }

    @Override
    public void load(InputStream inStream) throws IOException {
        getPropertiesToUse().load(inStream);
    }

    @Override
    public void store(Writer writer, String comments) throws IOException {
        getPropertiesToUse().store(writer, comments);
    }

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        getPropertiesToUse().store(out, comments);
    }

    @Override
    public void loadFromXML(InputStream in) throws IOException {
        getPropertiesToUse().loadFromXML(in);
    }

    @Override
    public void storeToXML(OutputStream os, String comment) throws IOException {
        getPropertiesToUse().storeToXML(os, comment);
    }

    @Override
    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        getPropertiesToUse().storeToXML(os, comment, encoding);
    }

    @Override
    public String getProperty(String key) {
        return getPropertiesToUse().getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return getPropertiesToUse().getProperty(key, defaultValue);
    }

    @Override
    public Enumeration<?> propertyNames() {
        return getPropertiesToUse().propertyNames();
    }

    @Override
    public Set<String> stringPropertyNames() {
        return getPropertiesToUse().stringPropertyNames();
    }

    @Override
    public void list(PrintStream out) {
        getPropertiesToUse().list(out);
    }

    @Override
    public void list(PrintWriter out) {
        getPropertiesToUse().list(out);
    }

    @Override
    public int size() {
        return getPropertiesToUse().size();
    }

    @Override
    public boolean isEmpty() {
        return getPropertiesToUse().isEmpty();
    }

    @Override
    public Enumeration<Object> keys() {
        return getPropertiesToUse().keys();
    }

    @Override
    public Enumeration<Object> elements() {
        return getPropertiesToUse().elements();
    }

    @Override
    public boolean contains(Object value) {
        return getPropertiesToUse().contains(value);
    }

    @Override
    public boolean containsValue(Object value) {
        return getPropertiesToUse().containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return getPropertiesToUse().containsKey(key);
    }

    @Override
    public Object get(Object key) {
        return getPropertiesToUse().get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return getPropertiesToUse().put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return getPropertiesToUse().remove(key);
    }

    @Override
    public void putAll(Map<?, ?> t) {
        getPropertiesToUse().putAll(t);
    }

    @Override
    public void clear() {
        getPropertiesToUse().clear();
    }

    @Override
    public String toString() {
        return getPropertiesToUse().toString();
    }

    @Override
    public Set<Object> keySet() {
        return getPropertiesToUse().keySet();
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return getPropertiesToUse().entrySet();
    }

    @Override
    public Collection<Object> values() {
        return getPropertiesToUse().values();
    }

    @Override
    public boolean equals(Object o) {
        return getPropertiesToUse().equals(o);
    }

    @Override
    public int hashCode() {
        return getPropertiesToUse().hashCode();
    }
}
