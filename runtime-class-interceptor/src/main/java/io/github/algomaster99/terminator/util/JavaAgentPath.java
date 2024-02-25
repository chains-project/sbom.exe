package io.github.algomaster99.terminator.util;

import io.github.algomaster99.terminator.index.RuntimeClassInterceptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JavaAgentPath {
    public static String getAgentPath() throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path runtimeClassInterceptorJar = Path.of(tempDir, "trace-collector.jar");

        try (InputStream runtimeClassInterceptorStream =
                RuntimeClassInterceptor.class.getResourceAsStream("/runtime-class-interceptor.jar")) {
            Files.copy(runtimeClassInterceptorStream, runtimeClassInterceptorJar, StandardCopyOption.REPLACE_EXISTING);
        }

        return runtimeClassInterceptorJar.toAbsolutePath().toString();
    }
}
