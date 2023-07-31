package io.github.algomaster99.terminator.commons.fingerprint;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

@JsonDeserialize(using = FingerprintDeserializer.class)
public interface Fingerprint {
    String className();

    String classFileVersion();

    String hash();

    String algorithm();
}

class FingerprintDeserializer extends JsonDeserializer<Fingerprint> {
    @Override
    public Fingerprint deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        ObjectNode root = mapper.readTree(p);
        Class<? extends Fingerprint> instanceClass = null;
        if (root.has("path")) {
            instanceClass = Jar.class;
        } else {
            instanceClass = Maven.class;
        }
        if (instanceClass == null) {
            return null;
        }
        return mapper.convertValue(root, instanceClass);
    }
}
