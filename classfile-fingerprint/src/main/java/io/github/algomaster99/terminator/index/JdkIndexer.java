package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.JdkClass;
import io.github.algomaster99.terminator.commons.fingerprint.JdkClassFinder;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
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

    Map<String, Set<ClassFileAttributes>> createOrMergeProvenances(
            Map<String, Set<ClassFileAttributes>> referenceProvenance) {
        System.out.println("--------------------");
        System.out.println("Indexing all JDK classes ...");
        List<JdkClass> jdkClasses = JdkClassFinder.listJdkClasses();
        jdkClasses.forEach(resource -> {
            byte[] classfileBytes = resource.bytes();
            String classfileVersion = ClassfileVersion.getVersion(classfileBytes);
            String hash = HashComputer.computeHash(classfileBytes);
            referenceProvenance.computeIfAbsent(
                    resource.name(),
                    k -> new HashSet<>(Set.of(new ClassFileAttributes(classfileVersion, hash, algorithm))));
        });
        return referenceProvenance;
    }
}
