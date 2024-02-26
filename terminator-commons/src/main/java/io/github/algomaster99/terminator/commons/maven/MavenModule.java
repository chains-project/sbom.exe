package io.github.algomaster99.terminator.commons.maven;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Model;

public class MavenModule {
    private final Model self;

    private final Path fileSystemPath;

    private final List<MavenModule> submodules = new ArrayList<>();

    MavenModule(Model self, Path fileSystemPath) {
        this.self = self;
        this.fileSystemPath = fileSystemPath;
    }

    public void addChild(MavenModule child) {
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
}
