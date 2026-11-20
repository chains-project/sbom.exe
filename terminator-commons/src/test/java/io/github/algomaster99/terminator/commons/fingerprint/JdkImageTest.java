package io.github.algomaster99.terminator.commons.fingerprint;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JdkImageTest {

    @Test
    void readsClassOnInterruptedThread() {
        // A class can be loaded on an interrupted thread, e.g. when an interrupted worker exits.
        Thread.currentThread().interrupt();
        try {
            assertThat(JdkImage.read(Object.class.getModule(), "java/lang/Object"))
                    .isNotNull();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void readsClassWhileAnotherThreadInterruptsIt() throws InterruptedException {
        // Another thread can interrupt this one during a read, e.g. when a pool shuts down its workers.
        LongAdder failedReads = new LongAdder();
        AtomicBoolean done = new AtomicBoolean();
        Thread reader = new Thread(() -> {
            for (int i = 0; i < 20_000; i++) {
                if (JdkImage.read(Object.class.getModule(), "java/util/concurrent/ForkJoinPool") == null) {
                    failedReads.increment();
                }
            }
            done.set(true);
        });
        reader.start();
        while (!done.get()) {
            reader.interrupt();
            Thread.onSpinWait();
        }
        reader.join();
        assertThat(failedReads.sum()).isZero();
    }

    @Test
    void readsClassOnlyFromJrtFsOfJdk(@TempDir Path dir) throws IOException {
        Path javaHome = Path.of(System.getProperty("java.home"));
        Path jrtFs = javaHome.resolve("lib/jrt-fs.jar");
        Path copy = Files.copy(jrtFs, dir.resolve("jrt-fs.jar"));
        String className = "jdk/internal/jrtfs/JrtPath";

        assertThat(JdkImage.readFromJrtFs(domainOf(jrtFs), className)).isNotEmpty();
        assertThat(JdkImage.readFromJrtFs(domainOf(copy), className)).isNull();
        // The digest does not check a JAR that is added to the JDK after startup.
        assertThat(JdkImage.readFromJrtFs(domainOf(javaHome.resolve("lib/evil.jar")), className))
                .isNull();
    }

    private static ProtectionDomain domainOf(Path location) throws IOException {
        return new ProtectionDomain(new CodeSource(location.toUri().toURL(), (Certificate[]) null), null);
    }
}
