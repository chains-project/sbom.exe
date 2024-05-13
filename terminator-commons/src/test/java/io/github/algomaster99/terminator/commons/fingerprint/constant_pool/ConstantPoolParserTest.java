package io.github.algomaster99.terminator.commons.fingerprint.constant_pool;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ConstantPoolParserTest {

    private static final Path CLASSFILE = Path.of("src/test/resources/classfile");

    @Test
    void proxy7ShouldBeEqualToProxy8InBehaviour() throws IOException {
        // arrange
        Path proxy7 = CLASSFILE.resolve("proxy").resolve("$Proxy7Runtime.class");
        Path proxy8 = CLASSFILE.resolve("proxy").resolve("$Proxy8Bomi.class");
        String newName = "Bar";
        byte[] proxy7BytesRewritten = new ConstantPoolParser(Files.readAllBytes(proxy7))
                .rewriteAllClassInfo(newName)
                .getBytecode();
        byte[] proxy8BytesRewritten = new ConstantPoolParser(Files.readAllBytes(proxy8))
                .rewriteAllClassInfo(newName)
                .getBytecode();

        // assert
        assertThat(proxy7BytesRewritten).isEqualTo(proxy8BytesRewritten);
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

    @Test
    void allFieldsShouldHaveTheSameName() throws IOException {
        // arrange
        Path pathToProxyClass = CLASSFILE.resolve("fooToBar").resolve("Foo.class");
        byte[] bytes = Files.readAllBytes(pathToProxyClass);
        ConstantPoolParser parser = new ConstantPoolParser(bytes);

        assertThat(fieldNames(parser)).containsOnly("x", "y");

        // act
        ConstantPoolParser rewritten = parser.rewriteAllFieldRef("z");

        // assert
        assertThat(fieldNames(rewritten)).containsOnly("z");
    }

    private static Set<String> fieldNames(ConstantPoolParser pool) {
        Map<Short, Constant_NameAndType> cpIndexToConstantNameAndType =
                ConstantPoolParser.setToMap(pool.nameAndTypeInfo);
        return pool.fieldRefInfo.stream()
                .map(f -> {
                    Constant_NameAndType nameAndType = cpIndexToConstantNameAndType.get(f.nameAndTypeIndex);
                    Constant_Utf8 utf8 = pool.cpIndexToConstantUtf8.get(nameAndType.nameIndex);
                    return utf8.bytes;
                })
                .collect(Collectors.toSet());
    }

    @Test
    void rewriteAllFieldRef() throws IOException {
        // arrange
        Path directoryWithIncorrectMethodMapping = Path.of("src/test/resources/classfile/fooToBar");
        Path foo = directoryWithIncorrectMethodMapping.resolve("Foo.class");

        // assert
        byte[] firstBytes = Files.readAllBytes(foo);
        ConstantPoolParser originalParser = new ConstantPoolParser(firstBytes);
        assertThat(fieldNames(originalParser)).containsOnly("x", "y");

        ConstantPoolParser transformedParser = originalParser.rewriteAllFieldRef("bar");
        assertThat(fieldNames(transformedParser)).containsOnly("bar");
    }

    @Test
    void orderShouldNotMatter() throws IOException {
        // arrange
        Path foo = CLASSFILE.resolve("fooToBar").resolve("Foo.class");
        Path bar = CLASSFILE.resolve("fooToBar").resolve("Bar.class");
        String newName = "terraform_mars";
        byte[] fooRewritten = new ConstantPoolParser(Files.readAllBytes(foo))
                .rewriteAllClassInfo(newName)
                .rewriteAllFieldRef(newName)
                .getBytecode();
        byte[] barRewritten = new ConstantPoolParser(Files.readAllBytes(bar))
                .rewriteAllClassInfo(newName)
                .rewriteAllFieldRef(newName)
                .getBytecode();

        // assert
        Arrays.sort(fooRewritten);
        Arrays.sort(barRewritten);
        assertThat(fooRewritten).isEqualTo(barRewritten);
    }

    @Test
    void proxy9_to_proxy13() throws IOException {
        // arrange
        Path proxy9 = CLASSFILE
                .resolve("proxy-class-with-incorrect-method-mapping")
                .resolve("$ProxyRuntimeProxy_CommandLine$Command_9.class");
        Path proxy13 = CLASSFILE
                .resolve("proxy-class-with-incorrect-method-mapping")
                .resolve("$ProxyIndexProxy_CommandLine$Command_13.class");
        String newName = "Bar";
        byte[] proxy9BytesRewritten = new ConstantPoolParser(Files.readAllBytes(proxy9))
                .rewriteAllClassInfo(newName)
                .rewriteAllFieldRef(newName)
                .getBytecode();
        byte[] proxy13BytesRewritten = new ConstantPoolParser(Files.readAllBytes(proxy13))
                .rewriteAllClassInfo(newName)
                .rewriteAllFieldRef(newName)
                .getBytecode();

        // assert
        Arrays.sort(proxy9BytesRewritten);
        Arrays.sort(proxy13BytesRewritten);
        assertThat(proxy9BytesRewritten).isEqualTo(proxy13BytesRewritten);
    }

    @Test
    void A_to_B() throws IOException {
        // arrange
        Path A = CLASSFILE.resolve("something-wrong-with-clint").resolve("A.class");
        Path B = CLASSFILE.resolve("something-wrong-with-clint").resolve("B.class");
        String newName = "Bar";
        byte[] ABytesRewritten = new ConstantPoolParser(Files.readAllBytes(A))
                .rewriteAllClassInfo(newName)
                .rewriteAllFieldRef(newName)
                .getBytecode();
        byte[] BBytesRewritten = new ConstantPoolParser(Files.readAllBytes(B))
                .rewriteAllClassInfo(newName)
                .rewriteAllFieldRef(newName)
                .getBytecode();

        // assert
        Arrays.sort(ABytesRewritten);
        Arrays.sort(BBytesRewritten);
        assertThat(ABytesRewritten).isEqualTo(BBytesRewritten);
    }

    @Nested
    class ChainOperations {
        @Test
        void rewriteAllFieldRef_then_rewriteAllClassInfo() throws IOException {
            // arrange
            Path directoryWithIncorrectMethodMapping = Path.of("src/test/resources/classfile/fooToBar");
            Path foo = directoryWithIncorrectMethodMapping.resolve("Foo.class");

            // assert
            byte[] firstBytes = Files.readAllBytes(foo);
            ConstantPoolParser originalParser = new ConstantPoolParser(firstBytes);
            assertThat(fieldNames(originalParser)).containsOnly("x", "y");
            assertThat(originalParser.thisClass).isEqualTo("Foo");

            ConstantPoolParser transformedParser =
                    originalParser.rewriteAllFieldRef("bar").rewriteAllClassInfo("fakeClazz");
            assertThat(fieldNames(transformedParser)).containsOnly("bar");
            assertThat(transformedParser.thisClass).isEqualTo("fakeClazz");
        }

        @Test
        void rewriteAllClassInfo_then_rewriteAllFieldRef() throws IOException {
            // arrange
            Path directoryWithIncorrectMethodMapping = Path.of("src/test/resources/classfile/fooToBar");
            Path foo = directoryWithIncorrectMethodMapping.resolve("Foo.class");

            // assert
            byte[] firstBytes = Files.readAllBytes(foo);
            ConstantPoolParser originalParser = new ConstantPoolParser(firstBytes);
            assertThat(fieldNames(originalParser)).containsOnly("x", "y");
            assertThat(originalParser.thisClass).isEqualTo("Foo");

            ConstantPoolParser transformedParser =
                    originalParser.rewriteAllClassInfo("fakeClazz").rewriteAllFieldRef("bar");
            assertThat(fieldNames(transformedParser)).containsOnly("bar");
            assertThat(transformedParser.thisClass).isEqualTo("fakeClazz");
        }
    }
}
