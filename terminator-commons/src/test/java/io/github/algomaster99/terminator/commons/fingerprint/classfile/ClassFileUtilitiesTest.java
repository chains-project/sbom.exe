package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class ClassFileUtilitiesTest {
    @Test
    void getNameForProxyClass() throws IOException {
        // arrange
        Path pathToProxyClass = Path.of("src/test/resources/classfile/$Proxy0Index.class");
        byte[] bytes = Files.readAllBytes(pathToProxyClass);

        // act
        String moreReadableName = ClassFileUtilities.getNameForProxyClass(bytes);

        // assert
        assertThat(moreReadableName).isEqualTo("Proxy_Retention");
    }
}
