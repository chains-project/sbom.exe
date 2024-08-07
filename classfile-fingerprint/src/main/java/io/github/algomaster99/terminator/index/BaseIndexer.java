package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * The class provides the base options for all the indexers.
 */
@CommandLine.Command()
public abstract class BaseIndexer implements Callable<Integer> {
    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    protected IndexFile indexFile;

    protected static class IndexFile {
        @CommandLine.Option(
                names = {"-o", "--output"},
                description = "Use this option if you want to create a new file with classfiles")
        protected File output;

        @CommandLine.Option(
                names = {"-i", "--input"},
                description = "Use this option if you want to use an existing file with classfiles")
        protected File input;
    }

    @CommandLine.Option(
            names = {"--algorithm"},
            description =
                    "The algorithm to use for computing the hash. See https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#messagedigest-algorithms.",
            defaultValue = "SHA-256",
            required = true)
    protected String algorithm;

    @Override
    public Integer call() throws Exception {
        if (indexFile.input != null) {
            Map<String, Set<ClassFileAttributes>> currentReferenceProvenance =
                    ParsingHelper.deserializeFingerprints(indexFile.input.toPath());
            Map<String, Set<ClassFileAttributes>> updatedReferenceProvenance =
                    createOrMergeProvenances(currentReferenceProvenance);
            ParsingHelper.serialiseFingerprints(updatedReferenceProvenance, indexFile.input.toPath());
            System.out.println(
                    String.format("Classes in %s: %d.", indexFile.input.getName(), updatedReferenceProvenance.size()));
            System.out.println("--------------------");
            return 0;
        }
        if (indexFile.output != null) {
            Map<String, Set<ClassFileAttributes>> updatedReferenceProvenance =
                    createOrMergeProvenances(new HashMap<>());
            ParsingHelper.serialiseFingerprints(updatedReferenceProvenance, indexFile.output.toPath());
            System.out.println(
                    String.format("Classes in %s: %d.", indexFile.output.getName(), updatedReferenceProvenance.size()));
            System.out.println("--------------------");
            return 0;
        }
        throw new IllegalArgumentException("Either --input or --output must be specified");
    }

    abstract Map<String, Set<ClassFileAttributes>> createOrMergeProvenances(
            Map<String, Set<ClassFileAttributes>> referenceProvenance);
}
