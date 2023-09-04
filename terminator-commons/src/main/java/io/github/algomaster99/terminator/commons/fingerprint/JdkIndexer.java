package io.github.algomaster99.terminator.commons.fingerprint;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The JdkIndexer class provides a utility to list all JDK classes by scanning the JDK used for the execution of the application.
 */
public class JdkIndexer {

    /**
     * Returns a list of all JDK classes. The list is populated by scanning the JDK used for the execution of this application.
     *
     * @return a list of all JDK classes, never null.
     */
    public static List<JdkClass> listJdkClasses() {
        List<JdkClass> jdkClasses = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph()
                .enableSystemJarsAndModules()
                .ignoreClassVisibility()
                .enableMemoryMapping()
                .scan()) {
            scanResult.getAllClasses().forEach(classInfo -> {
                Resource resource = classInfo.getResource();
                if (resource != null) {
                    byte[] byteBuffer;
                    try {
                        byteBuffer = resource.load();
                        jdkClasses.add(new JdkClass(classInfo.getName(), ByteBuffer.wrap(byteBuffer)));
                    } catch (IOException e) {
                        System.err.println("Error loading resource " + resource + ": " + e);
                    }
                }
            });
        }
        return jdkClasses;
    }
}
