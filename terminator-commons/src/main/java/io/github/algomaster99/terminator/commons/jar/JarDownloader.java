package io.github.algomaster99.terminator.commons.jar;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JarDownloader.class);
    private static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2";

    private JarDownloader() {}

    public static String getMavenJarUrl(String groupId, String artifactId, String version)
            throws IOException, InterruptedException {
        groupId = groupId.replace('.', '/');
        String url = MAVEN_CENTRAL_URL + "/" + groupId + "/" + artifactId + "/" + version + "/";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<String> candidates = Jsoup.parse(result.body()).select("a").stream()
                .map(e -> e.attr("href"))
                .toList();

        Optional<String> artifactJarName = candidates.stream()
                .filter(c -> c.endsWith(".jar"))
                .filter(c -> !c.contains("sources"))
                .filter(c -> !c.contains("javadoc"))
                .findFirst();

        if (artifactJarName.isPresent()) {
            return url + artifactJarName.get();
        } else {
            System.err.println("Could not find jar for " + url);
            LOGGER.warn("Could not find jar for {}:{}:{}", groupId, artifactId, version);
            return null;
        }
    }

    public static File getMavenJarFile(String groupId, String artifactId, String version)
            throws IOException, InterruptedException {
        String url = getMavenJarUrl(groupId, artifactId, version);
        if (url == null) {
            return null;
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        Path tempFile = File.createTempFile(String.format("%s-%s-%s", groupId, artifactId, version), ".jar")
                .toPath();
        HttpResponse<Path> result = client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));
        return result.body().toFile();
    }
}
