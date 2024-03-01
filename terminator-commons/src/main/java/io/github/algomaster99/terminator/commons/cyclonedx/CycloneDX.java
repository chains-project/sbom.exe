package io.github.algomaster99.terminator.commons.cyclonedx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converts the SBOM to a plain old java object
 */
public class CycloneDX {

    private CycloneDX() {}

    public static CycloneDXWrapper getPojo(String bom) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        CycloneDXWrapper wrapper = mapper.readValue(bom, CycloneDXWrapper.class);
        return wrapper;
    }
}
