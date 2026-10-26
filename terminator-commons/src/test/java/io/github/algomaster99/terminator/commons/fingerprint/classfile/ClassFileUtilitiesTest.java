package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class ClassFileUtilitiesTest {
    @Test
    void isProxyClass() throws IOException {
        // arrange
        byte[] proxy = Files.readAllBytes(Path.of("src/test/resources/classfile/$Proxy0Index.class"));
        byte[] generatedConstructorAccessor =
                Files.readAllBytes(Path.of("src/test/resources/classfile/GCAIndex_35.class"));

        // act + assert
        assertThat(ClassFileUtilities.isProxyClass(proxy)).isTrue();
        assertThat(ClassFileUtilities.isProxyClass(generatedConstructorAccessor))
                .isFalse();
    }

    @Test
    void getInterfacesOfProxyClass() throws IOException {
        // arrange
        byte[] proxy = Files.readAllBytes(Path.of("src/test/resources/classfile/$Proxy0Index.class"));

        // act
        List<String> interfaces = ClassFileUtilities.getInterfacesOfProxyClass(proxy);

        // assert
        assertThat(interfaces).containsExactly("java/lang/annotation/Retention");
    }

    @Test
    void getReadableNameOfProxyClass() throws IOException {
        // arrange
        Path pathToProxyClass = Path.of("src/test/resources/classfile/$Proxy0Index.class");
        byte[] bytes = Files.readAllBytes(pathToProxyClass);

        // act
        String moreReadableName = ClassFileUtilities.getReadableNameOfProxyClass(bytes);

        // assert
        assertThat(moreReadableName).isEqualTo("Proxy[java/lang/annotation/Retention]");
    }

    @Test
    void proxiesOfDifferentInterfacesHaveDifferentReadableNames() throws IOException {
        // arrange
        byte[] proxy = Files.readAllBytes(Path.of("src/test/resources/classfile/$Proxy0Index.class"));
        byte[] twoInterfaces = transform(proxy, c -> c.interfaces = List.of("Baz", "foo/Bar"));
        byte[] concatenatedInterface = transform(proxy, c -> c.interfaces = List.of("Bazfoo_Bar"));
        byte[] packagedInterface = transform(proxy, c -> c.interfaces = List.of("foo/Bar"));
        byte[] underscoredInterface = transform(proxy, c -> c.interfaces = List.of("foo_Bar"));

        // act + assert
        assertThat(ClassFileUtilities.getReadableNameOfProxyClass(twoInterfaces))
                .isNotEqualTo(ClassFileUtilities.getReadableNameOfProxyClass(concatenatedInterface));
        assertThat(ClassFileUtilities.getReadableNameOfProxyClass(packagedInterface))
                .isNotEqualTo(ClassFileUtilities.getReadableNameOfProxyClass(underscoredInterface));
    }

    @Test
    void getClassForWhichGeneratedAccessorIsFor() throws IOException {
        // arrange
        Path pathToGeneratedConstructorAccessor = Path.of("src/test/resources/classfile/GCAIndex_35.class");
        byte[] bytes = Files.readAllBytes(pathToGeneratedConstructorAccessor);

        // act
        String correspondingClassName = ClassFileUtilities.getClassForWhichGeneratedAccessorIsFor(bytes);

        // assert
        assertThat(correspondingClassName).isEqualTo("sun/security/x509/AuthorityKeyIdentifierExtension");
    }

    private static byte[] transform(byte[] bytes, Consumer<ClassNode> transformation) {
        ClassNode classNode = new ClassNode();
        new ClassReader(bytes).accept(classNode, 0);
        transformation.accept(classNode);
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
