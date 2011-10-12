package org.gridman.urlhandler.classpath;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author Jonathan Knight
 */
public class Handler extends URLStreamHandler {

    public Handler() {
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        URL realURL = Handler.class.getResource(url.getFile());
        return realURL.openConnection();
    }
}
