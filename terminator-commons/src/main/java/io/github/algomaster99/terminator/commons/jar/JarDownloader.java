package io.github.algomaster99.terminator.commons.jar;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLParameters;
import org.jsoup.Jsoup;

public class JarDownloader {

    private static final Map<String, String> repositoryUrls = Map.of(
            "mavenCentral", "https://repo1.maven.org/maven2/",
            "jboss", "https://repository.jboss.org/nexus/content/repositories/thirdparty-releases/");

    private JarDownloader() {}

    private static String getIndexPageOfRepository(String artifactUrl) throws IOException, InterruptedException {
        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setProtocols(new String[] {"TLSv1.2"});
        sslParameters.setNeedClientAuth(false);
        // Repositories may redirect, e.g. JBoss redirects its old Nexus 2 paths to Nexus 3.
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .sslParameters(sslParameters)
                .build();

        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(artifactUrl)).build();
        HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (result.statusCode() != 200) {
            return null;
        }
        return result.body();
    }

    private static String getArtifactUrl(String groupId, String artifactId, String version, String repositoryUrl) {
        groupId = groupId.replace('.', '/');
        return repositoryUrl + groupId + "/" + artifactId + "/" + version + "/";
    }

    /**
     * Returns the URL of the artifact's main jar, {@code <artifactId>-<version>.jar}. The listing also holds classifier
     * jars (e.g. {@code -android}, {@code -grooid}), which can sort before the main jar and contain different classes.
     */
    static String getUrlOfRequestedJar(
            String indexPageContent, String indexPageUrl, String artifactId, String version) {
        String mainJarName = artifactId + "-" + version + ".jar";
        // Repositories such as JBoss list absolute URLs, so compare only the file name.
        Optional<String> artifactJar = Jsoup.parse(indexPageContent).select("a").stream()
                .map(e -> e.attr("href"))
                .filter(href -> href.substring(href.lastIndexOf('/') + 1).equals(mainJarName))
                .findFirst();

        if (artifactJar.isPresent()) {
            String artifactJarName = artifactJar.get();
            // java.net.URI has the worst APIs ever
            if (artifactJarName.startsWith("https://") || artifactJarName.startsWith("http://")) {
                return artifactJarName;
            }
            return indexPageUrl + artifactJarName;
        } else {
            System.err.println("Could not find jar for " + indexPageUrl);
            return null;
        }
    }

    public static File getJarFile(String groupId, String artifactId, String version)
            throws IOException, InterruptedException {
        for (String repositoryUrl : repositoryUrls.values()) {
            String url = getArtifactUrl(groupId, artifactId, version, repositoryUrl);
            String indexPageContent = getIndexPageOfRepository(url);
            if (indexPageContent != null) {
                String jarUrl = getUrlOfRequestedJar(indexPageContent, url, artifactId, version);
                if (jarUrl != null) {
                    HttpClient client = HttpClient.newBuilder()
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .build();
                    HttpRequest request =
                            HttpRequest.newBuilder().uri(URI.create(jarUrl)).build();

                    Path tempFile = File.createTempFile(String.format("%s-%s-%s", groupId, artifactId, version), ".jar")
                            .toPath();
                    HttpResponse<Path> result = client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));
                    if (result.statusCode() != 200) {
                        // Do not return an error page as if it were the jar.
                        Files.deleteIfExists(tempFile);
                        continue;
                    }
                    return result.body().toFile();
                }
            }
        }
        return null;
    }
}
