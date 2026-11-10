package io.github.algomaster99.terminator.commons.jar;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JarDownloaderTest {

    @Test
    void mainJarIsPickedOverClassifierJarsListedBeforeIt() {
        // Maven Central's listing for org.yaml:snakeyaml:1.33, in its order.
        String url = "https://repo1.maven.org/maven2/org/yaml/snakeyaml/1.33/";
        String listing = "<a href=\"snakeyaml-1.33-android.jar\">x</a>"
                + "<a href=\"snakeyaml-1.33-javadoc.jar\">x</a>"
                + "<a href=\"snakeyaml-1.33-sources.jar\">x</a>"
                + "<a href=\"snakeyaml-1.33.jar\">x</a>";

        assertThat(JarDownloader.getUrlOfRequestedJar(listing, url, "snakeyaml", "1.33"))
                .isEqualTo(url + "snakeyaml-1.33.jar");
    }

    @Test
    void absoluteHrefsAreMatchedByFileName() {
        String dir = "https://repository.jboss.org/nexus/repository/thirdparty-releases/org/example/lib/1.0/";
        String listing = "<a href=\"" + dir + "lib-1.0-tests.jar\">x</a>" + "<a href=\"" + dir + "lib-1.0.jar\">x</a>";

        assertThat(JarDownloader.getUrlOfRequestedJar(listing, dir, "lib", "1.0"))
                .isEqualTo(dir + "lib-1.0.jar");
    }
}
