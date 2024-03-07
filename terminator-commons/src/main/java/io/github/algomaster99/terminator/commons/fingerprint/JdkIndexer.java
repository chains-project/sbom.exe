package io.github.algomaster99.terminator.commons.fingerprint;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import nonapi.io.github.classgraph.classpath.SystemJarFinder;

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
                .disableDirScanning()
                .disableJarScanning()
                .acceptLibOrExtJars()
                .acceptModules("jdk.*", "java.*")
                .ignoreClassVisibility()
                .enableMemoryMapping()
                .scan(); ) {
            scanResult.getAllClasses().forEach(classInfo -> {
                Resource resource = classInfo.getResource();
                if (resource != null) {
                    byte[] byteBuffer;
                    try {
                        byteBuffer = resource.load();
                        jdkClasses.add(
                                new JdkClass(classInfo.getName().replaceAll("\\.", "/"), ByteBuffer.wrap(byteBuffer)));
                    } catch (IOException e) {
                        System.err.println("Error loading resource " + resource + ": " + e);
                    }
                }
            });
        }
        return jdkClasses;
    }


    private static List<JdkClass> readJarFile(String jarFilePath) throws IOException {
        List<JdkClass> jdkClasses = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    byte[] byteBuffer = jarFile.getInputStream(entry).readAllBytes();
                    jdkClasses.add(new JdkClass(entry.getName().replace(".class", ""), ByteBuffer.wrap(byteBuffer)));
                }
            }
        }
        return jdkClasses;
    }
}
