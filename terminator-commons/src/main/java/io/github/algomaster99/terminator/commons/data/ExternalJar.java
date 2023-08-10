package io.github.algomaster99.terminator.commons.data;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@JsonDeserialize(using = ExternalJarDeserialize.class)
public record ExternalJar(File path) {}

class ExternalJarDeserialize extends JsonDeserializer<ExternalJar> {

    @Override
    public ExternalJar deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JacksonException {
        JsonNode node = jp.getCodec().readTree(jp);

        String configFile = String.valueOf(ctx.findInjectableValue("configFile", null, null));

        File absolutePathOfExternalJar = new File(configFile)
                .getParentFile()
                .toPath()
                // It trivially returns the path of external jar if it is absolute
                .resolve(Path.of(node.get("path").asText()))
                .toFile();

        return new ExternalJar(absolutePathOfExternalJar.getAbsoluteFile());
    }
}
