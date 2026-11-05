package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeClassInterceptorTest {

    @Test
    void compiledClassHasClassFile(@TempDir Path classes) throws IOException {
        Files.createDirectories(classes.resolve("org/apache/fontbox/ttf"));
        Files.createFile(classes.resolve("org/apache/fontbox/ttf/TrueTypeFont.class"));

        assertThat(RuntimeClassInterceptor.hasClassFile(classes.toUri().toURL(), "org/apache/fontbox/ttf/TrueTypeFont"))
                .isTrue();
    }

    @Test
    void generatedClassHasNoClassFile(@TempDir Path classes) throws IOException {
        Files.createDirectories(classes.resolve("org/apache/fontbox/ttf"));
        Files.createFile(classes.resolve("org/apache/fontbox/ttf/TrueTypeFont.class"));
        String mock = "org/apache/fontbox/ttf/TrueTypeFont$MockitoMock$4p5VBsG8";

        assertThat(RuntimeClassInterceptor.hasClassFile(classes.toUri().toURL(), mock))
                .isFalse();
        assertThat(RuntimeClassInterceptor.hasClassFile(null, mock)).isFalse();
    }

    @Test
    void classesInJarsAreAssumedToHaveClassFiles(@TempDir Path directory) throws IOException {
        Path jar = Files.createFile(directory.resolve("fontbox.jar"));

        assertThat(RuntimeClassInterceptor.hasClassFile(
                        jar.toUri().toURL(), "org/apache/fontbox/ttf/TrueTypeFont$MockitoMock$4p5VBsG8"))
                .isTrue();
    }
}
