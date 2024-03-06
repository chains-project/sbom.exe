package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        List<String> jdkIndex = Files.readAllLines(indexFile);

        String[] runtime_argsFirst = {
            "runtime", "-p", project.toString(), "-i", indexFile.toString(), "-mj", "basic-math", "--cleanup"
        };
        Index.main(runtime_argsFirst);
        List<String> first = Files.readAllLines(indexFile);

        String[] jdk_argsSecond = {"jdk", "-o", indexFile.toString()};
        Index.main(jdk_argsSecond);
        String[] runtime_argsSecond = {
            "runtime", "-p", project.toString(), "-i", indexFile.toString(), "-mj", "basic-math", "--cleanup"
        };
        Index.main(runtime_argsSecond);
        List<String> second = Files.readAllLines(indexFile);

        // assert
        // This ensures that the project compiles
        assertThat(first).size().isGreaterThan(jdkIndex.size());
        assertThat(first).isEqualTo(second);
    }
}
