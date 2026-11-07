package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

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
    void getClassForWhichGeneratedAccessorIsFor() throws IOException {
        // arrange
        Path pathToGeneratedConstructorAccessor = Path.of("src/test/resources/classfile/GCAIndex_35.class");
        byte[] bytes = Files.readAllBytes(pathToGeneratedConstructorAccessor);

        // act
        String correspondingClassName = ClassFileUtilities.getClassForWhichGeneratedAccessorIsFor(bytes);

        // assert
        assertThat(correspondingClassName).isEqualTo("sun/security/x509/AuthorityKeyIdentifierExtension");
    }

    @Test
    void getClassNameFromBuffer_recoversNameWhenDefinedWithNullName() {
        // A class defined via ClassLoader.defineClass(null, ...) - e.g. an XSLTC translet - carries its name only in
        // the bytecode, so the watchdog and indexer recover it from the buffer rather than the null defineClass arg.
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "die/verwandlung/malicious", null, "java/lang/Object", null);
        cw.visitEnd();

        assertThat(ClassFileUtilities.getClassNameFromBuffer(cw.toByteArray())).isEqualTo("die/verwandlung/malicious");
    }
}
