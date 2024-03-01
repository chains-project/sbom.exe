package io.github.algomaster99.terminator.commons.cyclonedx;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "specVersion")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Bom14Schema.class, name = "1.4"),
    @JsonSubTypes.Type(value = Bom15Schema.class, name = "1.5")
})
public interface CycloneDXWrapper {
    Metadata getMetadata();

    Set<Component> getComponents();
}
