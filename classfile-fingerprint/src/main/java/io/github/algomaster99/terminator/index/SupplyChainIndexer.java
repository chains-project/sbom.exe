package io.github.algomaster99.terminator.index;

import static io.github.algomaster99.terminator.commons.jar.JarScanner.goInsideJarAndUpdateFingerprints;

import io.github.algomaster99.terminator.commons.cyclonedx.Component;
import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDX;
import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDXWrapper;
import io.github.algomaster99.terminator.commons.cyclonedx.Metadata;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.jar.JarDownloader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
        name = "supply-chain",
        mixinStandardHelpOptions = true,
        description = "Create an index of the classfiles from SBOM")
public class SupplyChainIndexer extends BaseIndexer implements Callable<Integer> {

    @CommandLine.Option(
            names = {"-s", "--sbom"},
            required = true,
            description = "A valid CyclondeDX 1.4 or 1.5 SBOM")
    private Path sbomPath;

    @Override
    Map<String, Set<ClassFileAttributes>> createOrMergeProvenances(
            Map<String, Set<ClassFileAttributes>> referenceProvenance) {
        CycloneDXWrapper sbom;
        System.out.println("Indexing all supply chain classes ...");
        try {
            sbom = CycloneDX.getPojo(Files.readString(sbomPath));
        } catch (IOException e) {
            System.err.println("SBOM could not be parsed.");
            throw new RuntimeException(e);
        }
        if (sbom.getMetadata() != null) {
            processRootComponent(sbom, referenceProvenance);
        }
        processAllComponents(sbom, referenceProvenance);
        return referenceProvenance;
    }

    private void processRootComponent(
            CycloneDXWrapper sbom, Map<String, Set<ClassFileAttributes>> referenceProvenance) {
        Metadata metadata = sbom.getMetadata();
        if (metadata == null) {
            return;
        }
        Component rootComponent = metadata.getComponent();
        if (rootComponent == null) {
            return;
        }
        File jarFile;
        try {
            jarFile = JarDownloader.getJarFile(
                    rootComponent.getGroup(), rootComponent.getName(), rootComponent.getVersion());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        goInsideJarAndUpdateFingerprints(
                jarFile,
                referenceProvenance,
                algorithm,
                rootComponent.getGroup(),
                rootComponent.getName(),
                rootComponent.getVersion());
    }

    private void processAllComponents(
            CycloneDXWrapper sbom, Map<String, Set<ClassFileAttributes>> referenceProvenance) {
        for (Component component : sbom.getComponents()) {
            try {
                File jarFile =
                        JarDownloader.getJarFile(component.getGroup(), component.getName(), component.getVersion());
                if (jarFile == null) {
                    continue;
                }
                goInsideJarAndUpdateFingerprints(
                        jarFile,
                        referenceProvenance,
                        algorithm,
                        component.getGroup(),
                        component.getName(),
                        component.getVersion());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
