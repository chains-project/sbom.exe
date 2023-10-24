package io.github.algomaster99.terminator.commons.cyclonedx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converts the SBOM to a plain old java object
 */
public class CycloneDX {

    private CycloneDX() {}

    // TODO: Inherit both versions from a common schema
    public static Bom14Schema getPojo_1_4(String bom) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(bom, Bom14Schema.class);
    }

    public static Bom15Schema getPojo_1_5(String bom) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(bom, Bom15Schema.class);
    }
}
