package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RuntimeIndexerTest {

    @Test
    void runtimeIndexShouldBeDeterministic(@TempDir Path tempDir) throws IOException {
        // arrange
        Path indexFile = tempDir.resolve("r.json");
        Path project = Path.of("src", "test", "resources", "runtime-index", "basic-math");

        // act
        String[] jdk_argsFirst = {"jdk", "-o", indexFile.toString()};
        Index.main(jdk_argsFirst);
        byte[] jdkIndex = Files.readAllBytes(indexFile);

        String[] runtime_argsFirst = {
            "runtime", "-p", project.toString(), "-i", indexFile.toString(), "-mj", "basic-math", "--cleanup"
        };
        Index.main(runtime_argsFirst);
        byte[] first = Files.readAllBytes(indexFile);

        String[] jdk_argsSecond = {"jdk", "-o", indexFile.toString()};
        Index.main(jdk_argsSecond);
        String[] runtime_argsSecond = {
            "runtime", "-p", project.toString(), "-i", indexFile.toString(), "-mj", "basic-math", "--cleanup"
        };
        Index.main(runtime_argsSecond);
        byte[] second = Files.readAllBytes(indexFile);

        // assert
        // This ensures that the project compiles
        assertThat(Bomi.parseFrom(first).getClassFileCount())
                .isGreaterThan(Bomi.parseFrom(jdkIndex).getClassFileCount());
        assertThat(Bomi.parseFrom(first).getClassFileCount())
                .isEqualTo(Bomi.parseFrom(second).getClassFileCount());
        assertThat(Bomi.parseFrom(first).getClassFileList())
                .isEqualTo(Bomi.parseFrom(second).getClassFileList());
    }
}
