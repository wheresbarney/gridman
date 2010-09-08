package org.gridman.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This is a ChildFirst (also called ParentLast) ClassLoader.
 * It allows us to Sandbox things (in this case Coherence nodes).
 */
public class ChildFirstClassLoader extends URLClassLoader {

	Map<String, Class> loadedClasses = new HashMap<String, Class>();
	Map<String, URL> loadedResources = new HashMap<String, URL>();
    ClassLoader root;

    Properties properties = new Properties();

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

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties props) {
        properties.putAll(props);
    }

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

