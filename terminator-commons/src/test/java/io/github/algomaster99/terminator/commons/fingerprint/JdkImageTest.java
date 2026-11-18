package io.github.algomaster99.terminator.commons.fingerprint;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

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
        AtomicInteger failedReads = new AtomicInteger();
        AtomicBoolean done = new AtomicBoolean();
        Thread reader = new Thread(() -> {
            for (int i = 0; i < 20_000; i++) {
                if (JdkImage.read(Object.class.getModule(), "java/util/concurrent/ForkJoinPool") == null) {
                    failedReads.incrementAndGet();
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
        assertThat(failedReads).hasValue(0);
    }
}
