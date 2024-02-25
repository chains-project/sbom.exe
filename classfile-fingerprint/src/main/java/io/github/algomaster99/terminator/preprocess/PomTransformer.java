package io.github.algomaster99.terminator.preprocess;

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
    private final Path pom;
    private final Model model;

    public Model getModel() {
        return model;
    }

    public void writeTransformedPomInPlace() throws IOException {
        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileWriter(pom.toFile(), StandardCharsets.UTF_8), model);
    }

    public PomTransformer(Path pom) throws IOException, XmlPullParserException {
        this.pom = pom;
        MavenXpp3Reader reader = new MavenXpp3Reader();
        this.model = reader.read(new FileReader(pom.toFile(), StandardCharsets.UTF_8));
    }

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

        getOrCreateArgLine(surefireConfiguration);

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

    private static void getOrCreateArgLine(Xpp3Dom surefireConfiguration) {
        Xpp3Dom argLine = surefireConfiguration.getChild("argLine");
        if (argLine == null) {
            argLine = new Xpp3Dom("argLine");
            argLine.setValue("-javaagent:");
            surefireConfiguration.addChild(argLine);
        } else {
            argLine.setValue("-javaagent:" + " " + argLine.getValue());
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
