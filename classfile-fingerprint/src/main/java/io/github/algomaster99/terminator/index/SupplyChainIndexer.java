package io.github.algomaster99.terminator.index;

import static io.github.algomaster99.terminator.commons.jar.JarScanner.goInsideJarAndUpdateFingerprints;

import io.github.algomaster99.terminator.commons.cyclonedx.Component;
import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDX;
import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDXWrapper;
import io.github.algomaster99.terminator.commons.cyclonedx.Metadata;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import io.github.algomaster99.terminator.commons.jar.JarDownloader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
        name = "supply-chain",
        mixinStandardHelpOptions = true,
        description = "Create an index of the classfiles from SBOM")
public class SupplyChainIndexer extends BaseIndexer implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SupplyChainIndexer.class);

    @CommandLine.Option(
            names = {"-s", "--sbom"},
            required = true,
            description = "A valid CyclondeDX 1.4 or 1.5 SBOM")
    private Path sbomPath;

    @Override
    void createOrMergeBomi(Bomi.Builder bomiBuilder) {
        CycloneDXWrapper sbom;
        try {
            sbom = CycloneDX.getPojo(Files.readString(sbomPath));
        } catch (IOException e) {
            LOGGER.error("SBOM could not be parsed.");
            throw new RuntimeException(e);
        }
        if (sbom.getMetadata() != null) {
            LOGGER.debug("Processing root component");
            processRootComponent(sbom, bomiBuilder);
        }
        LOGGER.debug("Processing all components");
        processAllComponents(sbom, bomiBuilder);
    }

    private void processRootComponent(CycloneDXWrapper sbom, Bomi.Builder bomiBuilder) {
        Metadata metadata = sbom.getMetadata();
        if (metadata == null) {
            LOGGER.warn("Metadata is not present.");
            return;
        }
        Component rootComponent = metadata.getComponent();
        if (rootComponent == null) {
            LOGGER.warn("Root component is not present.");
            return;
        }
        File jarFile;
        try {
            jarFile = JarDownloader.getJarFile(
                    rootComponent.getGroup(), rootComponent.getName(), rootComponent.getVersion());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        goInsideJarAndUpdateFingerprints(jarFile, bomiBuilder);
    }

    private void processAllComponents(CycloneDXWrapper sbom, Bomi.Builder bomiBuilder) {
        for (Component component : sbom.getComponents()) {
            try {
                File jarFile =
                        JarDownloader.getJarFile(component.getGroup(), component.getName(), component.getVersion());
                if (jarFile == null) {
                    LOGGER.warn(
                            "Could not find jar for {}:{}:{}",
                            component.getGroup(),
                            component.getName(),
                            component.getVersion());
                    continue;
                }
                goInsideJarAndUpdateFingerprints(jarFile, bomiBuilder);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
