package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class ConstantPoolParserTest {

    private static final Path CLASSFILE = Path.of("src/test/resources/classfile");

    @Test
    void generatedConstructorAccessorIsRewritten() throws IOException {
        Path gca5Runtime = CLASSFILE
                .resolve("generated-constructor-accessor")
                .resolve("GeneratedConstructorAccessor5Runtime.class");
        String actualHash = HashComputer.computeHash(Files.readAllBytes(gca5Runtime));

        Path gca8Bomi =
                CLASSFILE.resolve("generated-constructor-accessor").resolve("GeneratedConstructorAccessor8Bomi.class");
        String expectedHash = HashComputer.computeHash(Files.readAllBytes(gca8Bomi));

        assertThat(actualHash).isEqualTo(expectedHash);
    }
}
