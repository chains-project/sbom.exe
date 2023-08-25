package io.github.algomaster99.terminator.commons.fingerprint;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import java.util.List;
import java.util.stream.Collectors;

public class JdkIndexer {

    /**
     * Returns a list of all jdk classes. The list is populated by scanning the jdk used for the execution of this application.
     * @return a list of all jdk classes, never null.
     */
    public static List<Resource> listJdkClasses() {
        try (ScanResult scanResult = new ClassGraph()
                .enableSystemJarsAndModules()
                .acceptPackages("java.*", "jdk.*", "oracle.*", "sun.*")
                .scan()) {
            return scanResult.getAllClasses().stream().map(v -> v.getResource()).collect(Collectors.toList());
        }
    }
}
