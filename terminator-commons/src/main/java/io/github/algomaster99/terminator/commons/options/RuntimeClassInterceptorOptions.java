package io.github.algomaster99.terminator.commons.options;

import java.lang.reflect.Field;
import java.nio.file.Path;

public class RuntimeClassInterceptorOptions {
    private Path output;

    /**
     * Root of the project under test. Only classes loaded from it, or generated at runtime, are recorded.
     */
    private Path project;

    /**
     * Default constructor useful for using setters to set the values
     */
    public RuntimeClassInterceptorOptions() {}

    /**
     * Constructor to parse the agent arguments
     */
    public RuntimeClassInterceptorOptions(String agentArgs) {
        String[] args = agentArgs.split(",");
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String key = split[0];
            String value = split[1];

            switch (key) {
                case "output":
                    output = Path.of(value).toAbsolutePath();
                    break;
                case "project":
                    project = Path.of(value).toAbsolutePath();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + key);
            }
        }
    }

    public Path getOutput() {
        return output;
    }

    public RuntimeClassInterceptorOptions setOutput(Path output) {
        this.output = output.toAbsolutePath();
        return this;
    }

    public Path getProject() {
        return project;
    }

    public RuntimeClassInterceptorOptions setProject(Path project) {
        this.project = project.toAbsolutePath();
        return this;
    }

    @Override
    public String toString() {
        StringBuilder agentsArgsBuilder = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                Object value = field.get(this);
                // Unset options are left out because the agent would parse "null" as a value.
                if (value == null) {
                    continue;
                }
                if (agentsArgsBuilder.length() > 0) {
                    agentsArgsBuilder.append(",");
                }
                agentsArgsBuilder.append(field.getName()).append("=").append(value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return agentsArgsBuilder.toString();
    }
}
