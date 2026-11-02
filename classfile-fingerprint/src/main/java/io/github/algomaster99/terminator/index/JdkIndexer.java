package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.JdkClass;
import io.github.algomaster99.terminator.commons.fingerprint.JdkClassFinder;
import io.github.algomaster99.terminator.commons.fingerprint.JdkDigest;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileUtilities;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
        name = "jdk",
        mixinStandardHelpOptions = true,
        description = "Create an index of the classfiles in JDK")
public class JdkIndexer extends BaseIndexer implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdkIndexer.class);

    @CommandLine.Option(
            names = {"--digest"},
            description = "Record one digest of the whole JDK instead of a hash of each of its classes")
    boolean digest;

    @CommandLine.Option(
            names = {"--java-home"},
            description = "The JDK to digest. Requires --digest. Defaults to the JDK that runs the indexer.")
    File javaHome;

    Map<String, Set<ClassFileAttributes>> createOrMergeProvenances(
            Map<String, Set<ClassFileAttributes>> referenceProvenance) {
        if (javaHome != null && !digest) {
            throw new IllegalArgumentException("--java-home requires --digest");
        }
        if (digest) {
            Path home = javaHome != null ? javaHome.toPath() : Path.of(System.getProperty("java.home"));
            System.out.println("--------------------");
            System.out.println("Digesting the JDK at " + home + " ...");
            referenceProvenance
                    .computeIfAbsent(JdkDigest.BOMI_KEY, k -> new HashSet<>())
                    .add(new ClassFileAttributes(
                            JdkDigest.version(home), JdkDigest.compute(home, algorithm), algorithm));
            return referenceProvenance;
        }
        System.out.println("--------------------");
        System.out.println("Indexing all JDK classes ...");
        List<JdkClass> jdkClasses = JdkClassFinder.listJdkClasses();
        jdkClasses.forEach(resource -> {
            byte[] classfileBytes = resource.bytes();
            String classfileVersion = ClassFileUtilities.getVersion(classfileBytes);
            String hash = HashComputer.computeHash(classfileBytes);
            referenceProvenance.computeIfAbsent(
                    resource.name(),
                    k -> new HashSet<>(Set.of(new ClassFileAttributes(classfileVersion, hash, algorithm))));
        });
        return referenceProvenance;
    }
}
