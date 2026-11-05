package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RuntimeClassInterceptorTest {

    @Test
    void mockitoAndByteBuddyClassesAreTestDoubles() {
        assertThat(RuntimeClassInterceptor.isTestDouble("org/apache/fontbox/ttf/TrueTypeFont$MockitoMock$4p5VBsG8"))
                .isTrue();
        assertThat(RuntimeClassInterceptor.isTestDouble(
                        "org/apache/fontbox/ttf/TrueTypeFont$MockitoMock$4p5VBsG8$auxiliary$nMHDji4K"))
                .isTrue();
        assertThat(RuntimeClassInterceptor.isTestDouble("foo/Bar$ByteBuddy$abc123"))
                .isTrue();
    }

    @Test
    void otherClassesAreNotTestDoubles() {
        assertThat(RuntimeClassInterceptor.isTestDouble("org/apache/fontbox/ttf/TrueTypeFont"))
                .isFalse();
        assertThat(RuntimeClassInterceptor.isTestDouble("java/lang/invoke/BoundMethodHandle$Species_LI"))
                .isFalse();
    }
}
