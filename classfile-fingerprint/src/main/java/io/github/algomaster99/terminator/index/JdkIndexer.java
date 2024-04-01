package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.JdkClass;
import io.github.algomaster99.terminator.commons.fingerprint.JdkClassFinder;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.BomiUtility;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.ClassFile;
import java.util.List;
import java.util.Optional;
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

    void createOrMergeBomi(Bomi.Builder bomiBuilder) {
        List<JdkClass> jdkClasses = JdkClassFinder.listJdkClasses();
        for (JdkClass resource : jdkClasses) {
            byte[] classfileBytes = resource.bytes();
            String classfileVersion = ClassfileVersion.getVersion(classfileBytes);
            String hash = HashComputer.computeHash(classfileBytes);
            ClassFile.Builder classFile = ClassFile.newBuilder().setClassName(resource.name());

            ClassFile.Attribute attribute = ClassFile.Attribute.newBuilder()
                    .setVersion(classfileVersion)
                    .setHash(hash)
                    .build();

            classFile.addAttribute(attribute);

            Optional<ClassFile> classFileCandidate = BomiUtility.isClassFilePresent(bomiBuilder, resource.name());

            if (classFileCandidate.isPresent()) {
                // ensure that JDK does not have multiple versions of the same class
                assert classFileCandidate.get().getAttributeList().stream()
                        .map(ClassFile.Attribute::getHash)
                        .allMatch(h -> h.equals(hash));
            } else {
                bomiBuilder.addClassFile(classFile);
            }
        }
    }
}
