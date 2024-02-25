package io.github.algomaster99.terminator.preprocess;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

public class PomTransformerTest {
    @Test
    void modifySurefirePlugin_shouldAddArgLine() throws IOException, XmlPullParserException {
        // arrange
        Path testPom = Path.of("src/test/resources/surefire/add-argline.xml");
        PomTransformer transformer = new PomTransformer(testPom);

        // act
        transformer.transform();

        // assert
        Model transformedModel = transformer.getModel();
        Plugin surefire = transformedModel.getBuild().getPlugins().get(0);
        assertThatSurefirePluginIsPresent(surefire);

        Xpp3Dom surefireConfiguration = (Xpp3Dom) surefire.getConfiguration();
        assertThat(surefireConfiguration).isNotNull();

        surefireConfiguration.removeChild(surefireConfiguration.getChild("failIfNoTests"));
        surefireConfiguration.removeChild(surefireConfiguration.getChild("testFailureIgnore"));
        assertThat(surefireConfiguration.getChildCount()).isEqualTo(1);

        Xpp3Dom argLine = surefireConfiguration.getChild("argLine");
        assertThat(argLine.getValue()).contains("javaagent");
    }

    @Test
    void modifySurefirePlugin_shouldAddSurefirePlugin() throws IOException, XmlPullParserException {
        // arrange
        Path testPom = Path.of("src/test/resources/surefire/add-surefire-plugin.xml");
        PomTransformer transformer = new PomTransformer(testPom);
        assertThat(transformer.getModel().getBuild().getPlugins()).hasSize(0);

        // act
        transformer.transform();

        // assert
        assertThatSurefirePluginIsPresent(
                transformer.getModel().getBuild().getPlugins().get(0));
    }

    @Test
    void modifySurefirePlugin_shouldModifyArgLine() throws XmlPullParserException, IOException {
        // arrange
        Path testPom = Path.of("src/test/resources/surefire/modify-argline.xml");
        PomTransformer transformer = new PomTransformer(testPom);
        Xpp3Dom surefireConfiguration =
                (Xpp3Dom) transformer.getModel().getBuild().getPlugins().get(0).getConfiguration();
        String originalArgLine = surefireConfiguration.getChild("argLine").getValue();

        // act
        transformer.transform();

        // assert
        assertThat(surefireConfiguration.getChild("argLine").getValue()).isEqualTo("-javaagent: " + originalArgLine);
    }

    @Test
    void modifySurefirePlugin_shouldAdd_failIfNoTests_and_testFailureIgnore()
            throws XmlPullParserException, IOException {
        // arrange
        Path testPom = Path.of("src/test/resources/surefire/add-failIfNoTests_testFailureIgnore.xml");
        PomTransformer transformer = new PomTransformer(testPom);
        Xpp3Dom originalSurefireConfiguration =
                (Xpp3Dom) transformer.getModel().getBuild().getPlugins().get(0).getConfiguration();
        assertThat(originalSurefireConfiguration).isNull();

        // act
        transformer.transform();

        // assert
        Model transformedModel = transformer.getModel();
        Xpp3Dom transformedSurefireConfiguration =
                (Xpp3Dom) transformedModel.getBuild().getPlugins().get(0).getConfiguration();
        assertThat(transformedSurefireConfiguration.getChildCount()).isEqualTo(3);

        String failIfNoTests =
                transformedSurefireConfiguration.getChild("failIfNoTests").getValue();
        assertThat(failIfNoTests).asBoolean().isFalse();
        String testFailureIgnore =
                transformedSurefireConfiguration.getChild("testFailureIgnore").getValue();
        assertThat(testFailureIgnore).asBoolean().isTrue();
    }

    @Test
    void modifySurefirePlugin_shouldModify_failIfNoTests_and_testFailureIgnore()
            throws XmlPullParserException, IOException {
        // arrange
        Path testPom = Path.of("src/test/resources/surefire/modify-failIfNoTests_testFailureIgnore.xml");
        PomTransformer transformer = new PomTransformer(testPom);
        Xpp3Dom surefireConfiguration =
                (Xpp3Dom) transformer.getModel().getBuild().getPlugins().get(0).getConfiguration();
        String originalFailIfNoTests =
                surefireConfiguration.getChild("failIfNoTests").getValue();
        assertThat(originalFailIfNoTests).asBoolean().isTrue();
        String originalTestFailureIgnore =
                surefireConfiguration.getChild("testFailureIgnore").getValue();
        assertThat(originalTestFailureIgnore).asBoolean().isFalse();

        // act
        transformer.transform();

        // assert
        assertThat(surefireConfiguration.getChildCount()).isEqualTo(3);

        String transformedFailIfNoTests =
                surefireConfiguration.getChild("failIfNoTests").getValue();
        assertThat(transformedFailIfNoTests).asBoolean().isFalse();
        String transformedTestFailureIgnore =
                surefireConfiguration.getChild("testFailureIgnore").getValue();
        assertThat(transformedTestFailureIgnore).asBoolean().isTrue();
    }

    private static void assertThatSurefirePluginIsPresent(Plugin surefire) {
        assertThat(surefire.getGroupId()).isEqualTo("org.apache.maven.plugins");
        assertThat(surefire.getArtifactId()).isEqualTo("maven-surefire-plugin");
        assertThat(surefire.getVersion()).isEqualTo("3.2.5");
    }
}
