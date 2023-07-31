package org.example;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.net.URL;
import java.net.URLClassLoader;

public class LocalFile {
    public static void main(String[] args) throws MalformedURLException {
        Path path = Path.of("external_source", "non-malicious.jar").toAbsolutePath();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { path.toUri().toURL() });
        try {
            Class<?> nonMaliciousClass = urlClassLoader.loadClass("NonMalicious");
            nonMaliciousClass.getMethod("main", String[].class).invoke(null, (Object) null);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}