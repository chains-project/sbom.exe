package io.github.algomaster99.terminator.commons.cyclonedx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CycloneDX {

    private CycloneDX() {}

    /**
     * Converts the SBOM to a plain old java object
     */
    public static Bom14Schema getPOJO(String bom) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(bom, Bom14Schema.class);
    }
}
