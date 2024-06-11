package io.github.algomaster99.terminator.commons.jar;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.net.ssl.SSLParameters;
import org.jsoup.Jsoup;

public class JarDownloader {

    private static final Map<String, String> repositoryUrls = Map.of(
            "mavenCentral",
            "https://repo1.maven.org/maven2/",
            "jboss",
            "https://repository.jboss.org/nexus/content/repositories/thirdparty-releases/",
            "localhost",
            "http://0.0.0.0:8081/");

    private JarDownloader() {}

    private static String getIndexPageOfRepository(String artifactUrl) throws IOException, InterruptedException {
        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setProtocols(new String[] {"TLSv1.2"});
        sslParameters.setNeedClientAuth(false);
        HttpClient client = HttpClient.newBuilder().sslParameters(sslParameters).build();

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

    private static String getUrlOfRequestedJar(String indexPageContent, String indexPageUrl) {
        List<String> candidates = Jsoup.parse(indexPageContent).select("a").stream()
                .map(e -> e.attr("href"))
                .collect(Collectors.toList());

        Optional<String> artifactJar = candidates.stream()
                .filter(c -> c.endsWith(".jar"))
                .filter(c -> !c.contains("sources"))
                .filter(c -> !c.contains("javadoc"))
                .filter(c -> !c.contains("tests"))
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
            if (groupId.contains("com.turn") && repositoryUrl.contains("maven")) {
                continue;
            }
            String url = getArtifactUrl(groupId, artifactId, version, repositoryUrl);
            String indexPageContent = getIndexPageOfRepository(url);
            if (indexPageContent != null) {
                String jarUrl = getUrlOfRequestedJar(indexPageContent, url);
                if (jarUrl != null) {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request =
                            HttpRequest.newBuilder().uri(URI.create(jarUrl)).build();

                    Path tempFile = File.createTempFile(String.format("%s-%s-%s", groupId, artifactId, version), ".jar")
                            .toPath();
                    HttpResponse<Path> result = client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));
                    return result.body().toFile();
                }
            }
        }
        return null;
    }
}
