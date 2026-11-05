package io.github.algomaster99.terminator.commons.cyclonedx;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "specVersion")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Bom15Schema.class, name = "1.5"),
    @JsonSubTypes.Type(value = Bom16Schema.class, name = "1.6")
})
public interface CycloneDXWrapper {
    BomMetadata getMetadata();

    Set<? extends BomComponent> getComponents();

    /**
     * Implemented by the generated {@code metadata} class of each schema version (see {@code javaInterfaces}).
     */
    interface BomMetadata {
        BomComponent getComponent();
    }

    /**
     * Implemented by the generated {@code component} class of each schema version (see {@code javaInterfaces}).
     */
    interface BomComponent {
        String getGroup();

        String getName();

        String getVersion();
    }
}
