package io.github.algomaster99.terminator.commons.maven;

import org.apache.maven.model.Model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MavenModule {
    private Model self;

    private Path fileSystemPath;

    private final List<MavenModule> children = new ArrayList<>();

    MavenModule(Model self, Path fileSystemPath) {
        this.self = self;
        this.fileSystemPath = fileSystemPath;
    }

    public void addChild(MavenModule child) {
        children.add(child);
    }

    public Model getSelf() {
        return self;
    }

    public List<MavenModule> getChildren() {
        return children;
    }

    public Path getFileSystemPath() {
        return fileSystemPath;
    }
}
