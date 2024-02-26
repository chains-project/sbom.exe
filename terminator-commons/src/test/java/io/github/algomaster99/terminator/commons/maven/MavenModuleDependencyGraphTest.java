package io.github.algomaster99.terminator.commons.maven;

import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MavenModuleDependencyGraphTest {
    @Test
    void createMavenModuleGraph_singleModule() throws XmlPullParserException, IOException {
        // arrange
        Path projectRoot = Path.of("src/test/resources/maven-modules/single-module").toAbsolutePath();

        // act
        MavenModule root = MavenModuleDependencyGraph.createMavenModuleGraph(projectRoot);

        // assert
        assertThat(root).isNotNull();

        assertThat(root.getSelf().getArtifactId()).isEqualTo("single-module");
        assertThat(root.getFileSystemPath()).isEqualTo(projectRoot);
        assertThat(root.getChildren()).hasSize(0);
    }

    @Test
    void createMavenModuleGraph_multiModule_singleDepth() throws XmlPullParserException, IOException {
        // arrange
        Path projectRoot = Path.of("src/test/resources/maven-modules/multi-module-single-depth").toAbsolutePath();

        // act
        MavenModule root = MavenModuleDependencyGraph.createMavenModuleGraph(projectRoot);

        // assert
        assertThat(root).isNotNull();

        assertThat(root.getSelf().getArtifactId()).isEqualTo("multi-module-single-depth");
        assertThat(root.getFileSystemPath()).isEqualTo(projectRoot);
        assertThat(root.getChildren()).hasSize(2);

        MavenModule module1 = root.getChildren().get(0);
        MavenModule module2 = root.getChildren().get(1);

        assertThat(Set.of(module1.getSelf().getArtifactId(), module2.getSelf().getArtifactId())).contains("m1", "m2");
        assertThat(Set.of(module1.getFileSystemPath(), module2.getFileSystemPath())).contains(projectRoot.resolve("m1"), projectRoot.resolve("m2"));

        assertThat(module1.getChildren()).hasSize(0);
        assertThat(module2.getChildren()).hasSize(0);
    }

    @Test
    void createMavenModuleGraph_multiModule_multipleDepth() throws XmlPullParserException, IOException {
        // arrange
        Path projectRoot = Path.of("src/test/resources/maven-modules/multi-module-multiple-depth").toAbsolutePath();

        // act
        MavenModule root = MavenModuleDependencyGraph.createMavenModuleGraph(projectRoot);

        // assert
        assertThat(root).isNotNull();

        assertThat(root.getSelf().getArtifactId()).isEqualTo("multi-module-multiple-depth");
        assertThat(root.getFileSystemPath()).isEqualTo(projectRoot);
        assertThat(root.getChildren()).hasSize(2);

        MavenModule module1 = root.getChildren().get(0);
        MavenModule module2 = root.getChildren().get(1);

        assertThat(Set.of(module1.getSelf().getArtifactId(), module2.getSelf().getArtifactId())).contains("m1", "m2");
        assertThat(Set.of(module1.getFileSystemPath(), module2.getFileSystemPath())).contains(projectRoot.resolve("m1"), projectRoot.resolve("m2"));

        assertThat(module1.getChildren()).hasSize(0);
        assertThat(module2.getChildren()).hasSize(1);

        MavenModule module21 = module2.getChildren().get(0);

        assertThat(module21.getSelf().getArtifactId()).isEqualTo("m21");
        assertThat(module21.getFileSystemPath()).isEqualTo(projectRoot.resolve("m2").resolve("m21"));
        assertThat(module21.getChildren()).hasSize(0);
    }
}
