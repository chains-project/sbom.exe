package io.github.algomaster99.terminator.commons.options;

import java.lang.reflect.Field;
import java.nio.file.Path;

public class RuntimeClassInterceptorOptions {
    private Path output;

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
                    output = Path.of(value);
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
        this.output = output;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder agentsArgsBuilder = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            try {
                agentsArgsBuilder.append(field.getName()).append("=").append(field.get(this));
                if (i < fields.length - 1) {
                    agentsArgsBuilder.append(",");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return agentsArgsBuilder.toString();
    }
}
