package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class ClassFileUtilitiesTest {
    @Test
    void getInterfacesOfProxyClass() throws IOException {
        // arrange
        Path pathToProxyClass = Path.of("src/test/resources/classfile/$Proxy0Index.class");
        byte[] bytes = Files.readAllBytes(pathToProxyClass);

        // act
        String moreReadableName = ClassFileUtilities.getInterfacesOfProxyClass(bytes);

        // assert
        assertThat(moreReadableName).isEqualTo("Retention");
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
}
