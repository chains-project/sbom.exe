package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import java.io.File;
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
            Bomi.Builder bomi = Bomi.newBuilder();
            Bomi currentBomi = ParsingHelper.deserializeFingerprints(indexFile.input.toPath());
            bomi.mergeFrom(currentBomi);
            createOrMergeBomi(bomi);
            ParsingHelper.serialiseFingerprints(bomi.build(), indexFile.input.toPath());
            return 0;
        }
        if (indexFile.output != null) {
            Bomi.Builder bomi = Bomi.newBuilder();
            createOrMergeBomi(bomi);
            ParsingHelper.serialiseFingerprints(bomi.build(), indexFile.output.toPath());
            return 0;
        }
        throw new IllegalArgumentException("Either --input or --output must be specified");
    }

    abstract void createOrMergeBomi(Bomi.Builder bomiBuilder);
}
