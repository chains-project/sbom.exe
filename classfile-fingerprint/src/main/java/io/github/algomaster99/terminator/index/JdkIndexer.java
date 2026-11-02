package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.JdkDigest;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
        name = "jdk",
        mixinStandardHelpOptions = true,
        description = "Record a digest of a JDK. Each run adds one JDK to the index.")
public class JdkIndexer extends BaseIndexer implements Callable<Integer> {

    @CommandLine.Option(
            names = {"--java-home"},
            description = "The JDK to digest. Defaults to the JDK that runs the indexer.")
    File javaHome;

    Map<String, Set<ClassFileAttributes>> createOrMergeProvenances(
            Map<String, Set<ClassFileAttributes>> referenceProvenance) {
        Path home = javaHome != null ? javaHome.toPath() : Path.of(System.getProperty("java.home"));
        System.out.println("--------------------");
        System.out.println("Digesting the JDK at " + home + " ...");
        referenceProvenance
                .computeIfAbsent(JdkDigest.BOMI_KEY, k -> new HashSet<>())
                .add(new ClassFileAttributes(JdkDigest.version(home), JdkDigest.compute(home, algorithm), algorithm));
        return referenceProvenance;
    }
}
