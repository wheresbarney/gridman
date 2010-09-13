package org.gridman.classloader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

public class PropertyDumper implements SandboxServer {
    public static final String FILE_PROPERTY = "org.gridman.PropertyDumper";

    public static final String[] ignored = {
            "line.separator",
            "idea.launcher.port"
    };

    public PropertyDumper() {
    }

    @Override
    public void start() {
        Properties properties = System.getProperties();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = System.getProperty(FILE_PROPERTY);

        File file = new File(tempDir, filename);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
            ArrayList<String> keySet = new ArrayList<String>(properties.stringPropertyNames());
            Collections.sort(keySet);
            for (String key : keySet) {
                if (!Arrays.asList(ignored).contains(key)) {
                    writer.println(key + "=" + properties.getProperty(key));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void shutdown() {

    }
}
