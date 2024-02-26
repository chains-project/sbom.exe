package io.github.algomaster99.terminator.commons.maven;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

public class MavenModule {
    private final MavenModule parent;

    private final Model self;

    private final Path fileSystemPath;

    private final List<MavenModule> submodules = new ArrayList<>();

    MavenModule(Model self, Path fileSystemPath, MavenModule parent) {
        this.self = self;
        this.fileSystemPath = fileSystemPath;
        this.parent = parent;
    }

    public void addSubmodule(MavenModule child) {
        submodules.add(child);
    }

    public Model getSelf() {
        return self;
    }

    public List<MavenModule> getSubmodules() {
        return submodules;
    }

    public Path getFileSystemPath() {
        return fileSystemPath;
    }

    public MavenModule findSubmodule(String artifactIdOfModule) {
        Queue<MavenModule> queue = new ArrayDeque<>();
        queue.add(topLevelParent());
        while (!queue.isEmpty()) {
            MavenModule module = queue.poll();
            if (module.getSelf().getArtifactId().equals(artifactIdOfModule)) {
                return module;
            }
            queue.addAll(module.getSubmodules());
        }
        return null;
    }

    public MavenModule topLevelParent() {
        if (parent == null) {
            return this;
        }
        return parent.topLevelParent();
    }

    public List<MavenModule> getSubmodulesThatAreDependencies() {
        List<MavenModule> subModulesThatAreDependencies = new ArrayList<>();
        List<Dependency> dependencies = self.getDependencies();
        for (Dependency dependency : dependencies) {
            String artifactId = dependency.getArtifactId();
            MavenModule submodule = findSubmodule(artifactId);
            if (submodule == null) {
                continue;
            }
            subModulesThatAreDependencies.add(submodule);
            submodule.getSubmodulesThatAreDependencies();
        }
        return subModulesThatAreDependencies;
    }

    /**
     * This is delegated to {@link Model#equals(Object)}.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MavenModule that = (MavenModule) obj;
        return self.equals(that.self);
    }

    @Override
    public int hashCode() {
        return self.hashCode();
    }
}
