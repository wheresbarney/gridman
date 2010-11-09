package org.gridman.testtools.classloader;

import org.apache.log4j.Logger;

import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.*;

/**
 * This is a ChildFirst (also called ParentLast) ClassLoader.
 * It allows us to Isolate things (eg Coherence nodes).
 */
public class ChildFirstClassLoader extends PropertyIsolatingClassLoader {
    private static final Logger logger = Logger.getLogger(ChildFirstClassLoader.class);

	private Map<String, Class> loadedClasses = new HashMap<String, Class>();
	private Map<String, URL> loadedResources = new HashMap<String, URL>();
    private ClassLoader root;

    public static ChildFirstClassLoader newInstance() throws Exception {
        logger.debug("ClassPath is" + System.getProperty("java.class.path"));
        String[] vals = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        List<URL> urls = new ArrayList<URL>();
        for (String val : vals) {
            logger.debug("Adding classpath : " + val);
            String ending = val.endsWith(".jar") ? "" : "/";
            urls.add(new URL("file:///" + val + ending));
        }

        ClassLoaderProperties.use();

        ClassLoader parentLoader = ChildFirstClassLoader.class.getClassLoader();
        ChildFirstClassLoader loader = new ChildFirstClassLoader(urls.toArray(new URL[urls.size()]), parentLoader);
        loader.setProperties(System.getProperties());
        return loader;
    }

    public static ChildFirstClassLoader newInstance(Properties localProperties) throws Exception {
        ChildFirstClassLoader loader = newInstance();
        loader.setProperties(localProperties);
        return loader;
    }

	public ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
		super(urls,null);
        while(parent.getParent() != null) {
            root = parent.getParent();
            parent = parent.getParent();
        }
        //System.out.println("Root is " + root);
	}

    /* I don't need these, someone else did
    public void addURL(URL url) {
		super.addURL(url);
	}

	public void addFileURL(File file) throws MalformedURLException {
		URL url = file.toURL();
		addURL(url);
	}
	*/

	public Class loadClass(String name) throws ClassNotFoundException {
		Class c = loadedClasses.get(name);
		if (c == null) {
            try {
                c = super.loadClass(name);               
            } catch(Throwable t) {
                // This catch is unfortunate, but necessary.
                // System.out.println("Ow!");
                c = root.loadClass(name);
            }
            loadedClasses.put(name, c);
		}
		return c;
	}

	protected PermissionCollection getPermissions(CodeSource codesource) {
		Permissions permissions = new Permissions();
		permissions.add(new AllPermission());
		return permissions;
	}

	public URL getResource(String name) {

		URL c = loadedResources.get(name);

		if (c == null) {
			c = findResource(name);
			loadedResources.put(name, c);
		}

		if (c == null) {
			c = super.getResource(name);
		}

		return c;
	}


}

