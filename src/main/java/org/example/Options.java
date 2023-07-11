package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sbom.Cyclonedx;

import java.io.File;
import java.io.IOException;

public class Options {
    private Cyclonedx sbom;

    public Options(String agentArgs) {
        String[] args = agentArgs.split(",");
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String key = split[0];
            String value = split[1];

            switch (key) {
                case "sbom":
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        this.sbom = objectMapper.readValue(new File(value), Cyclonedx.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + key);
            }
        }
    }

    public Cyclonedx getSbom() {
        return sbom;
    }
}
