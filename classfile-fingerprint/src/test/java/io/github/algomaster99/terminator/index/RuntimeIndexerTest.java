package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

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
        String[] runtime_argsFirst = {
            "runtime", "-p", project.toString(), "-i", indexFile.toString(), "-mj", "basic-math", "--cleanup"
        };
        Index.main(runtime_argsFirst);
        String contentFirst = Files.readString(indexFile);

        String[] jdk_argsSecond = {"jdk", "-o", indexFile.toString()};
        Index.main(jdk_argsSecond);
        String[] runtime_argsSecond = {
            "runtime", "-p", project.toString(), "-i", indexFile.toString(), "-mj", "basic-math", "--cleanup"
        };
        Index.main(runtime_argsSecond);
        String contentSecond = Files.readString(indexFile);

        // assert
        assertThat(contentFirst).isEqualTo(contentSecond);
    }

    @Test
    void pdfbox_indexShouldBeDeterministic(@TempDir Path tempDir) throws IOException {
        // arrange
        Path indexFile = tempDir.resolve("r.json");
        Path project = Path.of("src", "test", "resources", "runtime-index", "pdfbox");

        // act
        String[] jdk_argsFirst = {"jdk", "-o", indexFile.toString()};
        Index.main(jdk_argsFirst);
        String[] runtime_argsFirst = {
            "runtime", "-p", project.toString(), "-i", indexFile.toString(), "-mj", "pdfbox", "--cleanup"
        };
        Index.main(runtime_argsFirst);
        String contentFirst = Files.readString(indexFile);

        String[] jdk_argsSecond = {"jdk", "-o", indexFile.toString()};
        Index.main(jdk_argsSecond);
        String[] runtime_argsSecond = {
            "runtime", "-p", project.toString(), "-i", indexFile.toString(), "-mj", "pdfbox", "--cleanup"
        };
        Index.main(runtime_argsSecond);
        String contentSecond = Files.readString(indexFile);

        // assert
        assertThat(contentFirst).isEqualTo(contentSecond);
    }
}
