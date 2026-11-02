package io.github.algomaster99.terminator.commons.fingerprint;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParsingHelperTest {

    @Test
    void writesClassesAndAttributesInSortedOrder(@TempDir Path tempDir) throws IOException {
        // arrange
        ClassFileAttributes first = new ClassFileAttributes("55.0", "aa", "SHA-256");
        ClassFileAttributes second = new ClassFileAttributes("55.0", "bb", "SHA-256");
        Map<String, Set<ClassFileAttributes>> loadedFirstToLast = new LinkedHashMap<>();
        loadedFirstToLast.put("b/B", new LinkedHashSet<>(List.of(second, first)));
        loadedFirstToLast.put("a/A", new LinkedHashSet<>(List.of(first)));
        Path bomi = tempDir.resolve("bomi.jsonl");

        // act
        ParsingHelper.serialiseFingerprints(loadedFirstToLast, bomi);

        // assert
        List<String> lines = Files.readAllLines(bomi);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).startsWith("{\"a/A\":");
        assertThat(lines.get(1)).startsWith("{\"b/B\":").containsSubsequence("\"aa\"", "\"bb\"");
        assertThat(ParsingHelper.deserializeFingerprints(bomi)).isEqualTo(loadedFirstToLast);
    }
}
