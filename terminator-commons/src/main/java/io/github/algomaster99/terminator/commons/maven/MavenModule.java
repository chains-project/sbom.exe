package io.github.algomaster99.terminator.commons.maven;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Model;

public class MavenModule {
    private final Model self;

    private final Path fileSystemPath;

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
