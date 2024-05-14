package io.github.algomaster99.terminator.commons.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

public class MavenModuleTest {
    @Test
    void createMavenModuleGraph_singleModule() throws XmlPullParserException, IOException {
        // arrange
        Path projectRoot =
                Path.of("src/test/resources/maven-modules/single-module").toAbsolutePath();

        // act
        MavenModule root = MavenModule.createMavenModuleGraph(projectRoot);

        // assert
        assertThat(root).isNotNull();

        assertThat(root.getSelf().getArtifactId()).isEqualTo("single-module");
        assertThat(root.getFileSystemPath()).isEqualTo(projectRoot);
        assertThat(root.getSubmodules()).hasSize(0);
    }

    @Test
    void createMavenModuleGraph_multiModule_singleDepth() throws XmlPullParserException, IOException {
        // arrange
        Path projectRoot = Path.of("src/test/resources/maven-modules/multi-module-single-depth")
                .toAbsolutePath();

        // act
        MavenModule root = MavenModule.createMavenModuleGraph(projectRoot);

        // assert
        assertThat(root).isNotNull();

        assertThat(root.getSelf().getArtifactId()).isEqualTo("multi-module-single-depth");
        assertThat(root.getFileSystemPath()).isEqualTo(projectRoot);
        assertThat(root.getSubmodules()).hasSize(2);

        MavenModule module1 = root.getSubmodules().get(0);
        MavenModule module2 = root.getSubmodules().get(1);

        assertThat(Set.of(module1.getSelf().getArtifactId(), module2.getSelf().getArtifactId()))
                .contains("m1", "m2");
        assertThat(Set.of(module1.getFileSystemPath(), module2.getFileSystemPath()))
                .contains(projectRoot.resolve("m1"), projectRoot.resolve("m2"));

        assertThat(module1.getSubmodules()).hasSize(0);
        assertThat(module2.getSubmodules()).hasSize(0);
    }

    @Test
    void createMavenModuleGraph_multiModule_multipleDepth() throws XmlPullParserException, IOException {
        // arrange
        Path projectRoot = Path.of("src/test/resources/maven-modules/multi-module-multiple-depth")
                .toAbsolutePath();

        // act
        MavenModule root = MavenModule.createMavenModuleGraph(projectRoot);

        // assert
        assertThat(root).isNotNull();

        assertThat(root.getSelf().getArtifactId()).isEqualTo("multi-module-multiple-depth");
        assertThat(root.getFileSystemPath()).isEqualTo(projectRoot);
        assertThat(root.getSubmodules()).hasSize(2);

        MavenModule module1 = root.getSubmodules().get(0);
        MavenModule module2 = root.getSubmodules().get(1);

        assertThat(Set.of(module1.getSelf().getArtifactId(), module2.getSelf().getArtifactId()))
                .contains("m1", "m2");
        assertThat(Set.of(module1.getFileSystemPath(), module2.getFileSystemPath()))
                .contains(projectRoot.resolve("m1"), projectRoot.resolve("m2"));

        assertThat(module1.getSubmodules()).hasSize(0);
        assertThat(module2.getSubmodules()).hasSize(1);

        MavenModule module21 = module2.getSubmodules().get(0);

        assertThat(module21.getSelf().getArtifactId()).isEqualTo("m21");
        assertThat(module21.getFileSystemPath())
                .isEqualTo(projectRoot.resolve("m2").resolve("m21"));
        assertThat(module21.getSubmodules()).hasSize(0);
    }

    @Test
    void createMavenModuleGraph_submoduleAsDependency() throws XmlPullParserException, IOException {
        // arrange
        Path projectRoot = Path.of("src/test/resources/maven-modules/submodule-as-dependency")
                .toAbsolutePath();

        // act
        MavenModule root = MavenModule.createMavenModuleGraph(projectRoot);

        // assert
        assertThat(root).isNotNull();

        assertThat(root.getSelf().getArtifactId()).isEqualTo("submodule-as-dependency");
        assertThat(root.getFileSystemPath()).isEqualTo(projectRoot);
        assertThat(root.getSubmodules()).hasSize(2);

        MavenModule module1 = root.getSubmodules().get(0);
        MavenModule module2 = root.getSubmodules().get(1);

        assertThat(module1.getSubmodulesThatAreDependencies()).isEqualTo(Set.of(module2));
    }

    @Test
    void createMavenModuleGraph_pdfbox() throws XmlPullParserException, IOException {
        // arrange
        Path projectRoot = Path.of("src/test/resources/maven-modules/pdfbox").toAbsolutePath();

        // act
        MavenModule root = MavenModule.createMavenModuleGraph(projectRoot);

        // assert
        assertThat(root).isNotNull();
        MavenModule pdfboxApp = root.getSubmodules().get(6);
        assertThat(pdfboxApp.getSelf().getArtifactId()).isEqualTo("pdfbox-app");

        Set<MavenModule> submodulesThatAreDependencies = pdfboxApp.getSubmodulesThatAreDependencies();
        assertThat(submodulesThatAreDependencies).hasSize(5);
        assertThat(submodulesThatAreDependencies.stream().map(m -> m.getSelf().getArtifactId()))
                .containsOnly("pdfbox-tools", "pdfbox-debugger", "pdfbox", "fontbox", "pdfbox-io");
    }
}
