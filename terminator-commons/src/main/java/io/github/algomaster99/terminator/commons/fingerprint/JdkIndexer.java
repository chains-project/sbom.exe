package io.github.algomaster99.terminator.commons.fingerprint;

import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JdkIndexer class provides a utility to list all JDK classes by scanning the JDK used for the execution of the application.
 */
public class JdkIndexer {

    private static final Logger logger = LoggerFactory.getLogger(JdkIndexer.class);

    /**
     * Returns a list of all JDK classes. The list is populated by scanning the JDK used for the execution of this application.
     *
     * @return a list of all JDK classes, never null.
     */
    public static List<JdkClass> listJdkClasses() {
        List<JdkClass> jdkClasses = new ArrayList<>();
        ModuleFinder.ofSystem().findAll().forEach(mr -> {
            try {
                try (ModuleReader reader = mr.open()) {
                    for (String resource : reader.list().toList()) {
                        if (!resource.endsWith(".class")) {
                            continue;
                        }
                        Optional<ByteBuffer> contents = reader.read(resource);
                        if (contents.isPresent()) {
                            jdkClasses.add(new JdkClass(resource, contents.get()));
                        } else {
                            logger.atWarn()
                                    .log(
                                            "Could not read resource {} from module {}",
                                            resource,
                                            mr.descriptor().name());
                        }
                    }
                }
            } catch (Throwable e) {
                logger.atError()
                        .setCause(e)
                        .log("Error while reading module {}", mr.descriptor().name());
            }
        });
        return jdkClasses;
    }
}
