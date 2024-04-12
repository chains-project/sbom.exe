package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.constant_pool.ConstantPoolParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class ConstantPoolParserTest {

    private static final Path CLASSFILE = Path.of("src/test/resources/classfile");

    @Test
    void proxy7ShouldBeEqualToProxy8InBehaviour() throws IOException {
        // arrange
        Path proxy7 = CLASSFILE.resolve("proxy").resolve("$Proxy7Runtime.class");
        Path proxy8 = CLASSFILE.resolve("proxy").resolve("$Proxy8Bomi.class");
        String newName = "Bar";
        byte[] proxy7BytesRewritten = ConstantPoolParser.rewriteAllClassInfo(Files.readAllBytes(proxy7), newName);
        byte[] proxy8BytesRewritten = ConstantPoolParser.rewriteAllClassInfo(Files.readAllBytes(proxy8), newName);

        // assert
        assertThat(proxy7BytesRewritten).isEqualTo(proxy8BytesRewritten);
    }

    @Test
    void fooShouldBeConvertedToBar() throws IOException {
        // arrange
        Path foo = CLASSFILE.resolve("fooToBar").resolve("Foo.class");
        byte[] fooBytes = Files.readAllBytes(foo);
        String newName = "Bar";
        byte[] barBytes = ConstantPoolParser.rewriteAllClassInfo(Files.readAllBytes(foo), newName);

        // act
        int fooLength = fooBytes.length;
        int barLength = barBytes.length;

        // assert
        assertThat(fooLength).isNotEqualTo(barLength);
        assertThat(fooLength
                        - "java/lang/Object".getBytes().length // "Foo" extends "java/lang/Object"
                        - "Foo".getBytes().length // "Foo" class name
                        + newName.getBytes().length * 2) // "Bar" replaces both classes
                .isEqualTo(barLength);
    }

    @Test
    void generatedConstructorAccessorIsRewritten() throws IOException {
        // arrange
        Path gca5Runtime = CLASSFILE
                .resolve("generated-constructor-accessor")
                .resolve("GeneratedConstructorAccessor5Runtime.class");
        Path gca8Bomi =
                CLASSFILE.resolve("generated-constructor-accessor").resolve("GeneratedConstructorAccessor8Bomi.class");

        // act
        String actualHash = HashComputer.computeHash(Files.readAllBytes(gca5Runtime));
        String expectedHash = HashComputer.computeHash(Files.readAllBytes(gca8Bomi));

        // assert
        assertThat(actualHash).isEqualTo(expectedHash);
    }
}
