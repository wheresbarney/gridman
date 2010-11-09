package org.gridman.testtools;

import org.gridman.testtools.classloader.ClassloaderLifecycle;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class PropertyDumper implements ClassloaderLifecycle {
    public static final String FILE_PROPERTY = "org.gridman.PropertyDumper";

    public static final String[] ignored = {
            "line.separator",
            "idea.launcher.port"
    };

    private boolean done = false;

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
        done = true;
    }

    @Override
    public boolean isStarted() {
        return done;
    }

    @Override
    public void shutdown() {

    }
}
