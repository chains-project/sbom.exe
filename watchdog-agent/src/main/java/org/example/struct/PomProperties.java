package org.example.struct;

public class PomProperties {
    private final String groupID;
    private final String artifactID;
    private final String version;

    public PomProperties(String pomPropertiesContent) {
        String[] lines = pomPropertiesContent.split("\n");
        String groupID = null;
        String artifactID = null;
        String version = null;
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            } else if (groupID == null && line.startsWith("groupId=")) {
                groupID = line.substring("groupId=".length());
            } else if (artifactID == null && line.startsWith("artifactId=")) {
                artifactID = line.substring("artifactId=".length());
            } else if (version == null && line.startsWith("version=")) {
                version = line.substring("version=".length());
            } else {
                throw new RuntimeException("Invalid pom.properties file");
            }
        }
        this.groupID = groupID;
        this.artifactID = artifactID;
        this.version = version;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getArtifactID() {
        return artifactID;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PomProperties)) {
            return false;
        }
        PomProperties other = (PomProperties) o;
        return other.groupID.equals(this.groupID) && other.artifactID.equals(this.artifactID) && other.version.equals(this.version);
    }

    @Override
    public int hashCode() {
        return groupID.hashCode() + artifactID.hashCode() + version.hashCode();
    }
}
