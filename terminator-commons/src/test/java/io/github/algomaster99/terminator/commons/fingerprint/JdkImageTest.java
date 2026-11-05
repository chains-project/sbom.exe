package io.github.algomaster99.terminator.commons.fingerprint;

import static org.assertj.core.api.Assertions.assertThat;

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
}
