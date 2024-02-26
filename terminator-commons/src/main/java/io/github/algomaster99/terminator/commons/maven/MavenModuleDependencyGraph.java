package io.github.algomaster99.terminator.commons.maven;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class MavenModuleDependencyGraph {
    // create dependency graph of maven modules

    public static MavenModule createMavenModuleGraph(Path projectRoot) throws IOException, XmlPullParserException {
        Path rootPom = projectRoot.resolve("pom.xml");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model rootModel = reader.read(new FileReader(rootPom.toFile()));

        MavenModule root = new MavenModule(rootModel, projectRoot.toAbsolutePath(), null);

        List<String> submodules = rootModel.getModules();

        for (String module : submodules) {
            Path modulePath = projectRoot.resolve(module);
            MavenXpp3Reader moduleReader = new MavenXpp3Reader();
            Model moduleModel = moduleReader.read(
                    new FileReader(modulePath.resolve("pom.xml").toFile()));
            MavenModule mavenModule = new MavenModule(moduleModel, modulePath, root);
            if (moduleModel.getModules() != null) {
                List<String> childModules = moduleModel.getModules();
                List<MavenModule> children = childModules.stream()
                        .map(childModule -> {
                            try {
                                return createMavenModuleGraph(modulePath.resolve(childModule));
                            } catch (IOException | XmlPullParserException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toList();
                children.forEach(mavenModule::addSubmodule);
            }
            root.addSubmodule(mavenModule);
        }
        return root;
    }
}
