package io.github.algomaster99.terminator.preprocess;

import static io.github.algomaster99.terminator.util.JavaAgentPath.getAgentPath;

import io.github.algomaster99.terminator.commons.options.RuntimeClassInterceptorOptions;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class PomTransformer {
    static String AGENT_JAR;

    static {
        try {
            AGENT_JAR = getAgentPath();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not fetch trace-collector.jar. Please package `trace-collector` module again.");
        }
    }

    private final Path pom;
    private final Model model;

    private final RuntimeClassInterceptorOptions options;

    /**
     * Used for getting the transformed model.
     */
    public Model getModel() {
        return model;
    }

    /**
     * Writes the transformed model to the pom file.
     */
    public void writeTransformedPomInPlace() throws IOException {
        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileWriter(pom.toFile(), StandardCharsets.UTF_8), model);
    }

    public PomTransformer(Path pom, RuntimeClassInterceptorOptions options) throws IOException, XmlPullParserException {
        this.pom = pom;
        this.options = options;
        MavenXpp3Reader reader = new MavenXpp3Reader();
        this.model = reader.read(new FileReader(pom.toFile(), StandardCharsets.UTF_8));
    }

    /**
     * Transforms the model to add the Java agent to the surefire plugin.
     */
    public void transform() throws XmlPullParserException, IOException {
        modifySurefirePlugin();
    }

    private void modifySurefirePlugin() {
        Build build = model.getBuild();
        if (build == null) {
            build = new Build();
            model.setBuild(build);
        }

        Optional<Plugin> candidate = build.getPlugins().stream()
                .filter(plugin -> "maven-surefire-plugin".equals(plugin.getArtifactId()))
                .findFirst();

        Plugin surefirePlugin;
        if (candidate.isPresent()) {
            surefirePlugin = candidate.get();
        } else {
            surefirePlugin = new Plugin();
            surefirePlugin.setGroupId("org.apache.maven.plugins");
            surefirePlugin.setArtifactId("maven-surefire-plugin");
            surefirePlugin.setVersion("3.2.5");
            build.addPlugin(surefirePlugin);
        }

        Xpp3Dom surefireConfiguration = getOrCreateConfiguration(surefirePlugin);

        modifyOrCreateArgLine(surefireConfiguration, options);

        // Some modules may not have any tests, so we prevent its build from failing.
        modifyOrCreate("failIfNoTests", "false", surefireConfiguration);
        // The build should continue even if a test fails because we still need the data about classes loaded.
        modifyOrCreate("testFailureIgnore", "true", surefireConfiguration);
    }

    private static Xpp3Dom getOrCreateConfiguration(Plugin surefirePlugin) {
        Xpp3Dom configuration = (Xpp3Dom) surefirePlugin.getConfiguration();
        if (configuration == null) {
            configuration = new Xpp3Dom("configuration");
            surefirePlugin.setConfiguration(configuration);
        }
        return configuration;
    }

    private static void modifyOrCreateArgLine(Xpp3Dom surefireConfiguration, RuntimeClassInterceptorOptions options) {
        Xpp3Dom argLine = surefireConfiguration.getChild("argLine");
        if (argLine == null) {
            argLine = new Xpp3Dom("argLine");
            argLine.setValue("-javaagent:" + AGENT_JAR + "=" + options.toString());
            surefireConfiguration.addChild(argLine);
        } else {
            argLine.setValue("-javaagent:" + AGENT_JAR + "=" + options.toString() + " " + argLine.getValue());
        }
    }

    private static void modifyOrCreate(String attribute, String value, Xpp3Dom configuration) {
        Xpp3Dom attributeNode = configuration.getChild(attribute);
        if (attributeNode == null) {
            attributeNode = new Xpp3Dom(attribute);
            attributeNode.setValue(value);
            configuration.addChild(attributeNode);
        } else {
            attributeNode.setValue(value);
        }
    }
}
