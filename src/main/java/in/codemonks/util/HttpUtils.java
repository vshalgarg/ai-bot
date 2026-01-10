package in.codemonks.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtils {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static String post(String url, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return response.body();
        } else {
            throw new RuntimeException("HTTP POST failed: " + status + " - " + response.body());
        }
    }

    public static String get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return response.body();
        } else {
            throw new RuntimeException("HTTP GET failed: " + status + " - " + response.body());
        }
    }
}
