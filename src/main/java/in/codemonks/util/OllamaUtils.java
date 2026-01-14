package in.codemonks.util;

import com.fasterxml.jackson.databind.*;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OllamaUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String chat(String prompt, String model, String baseUrl) throws Exception {

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/chat/completions"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        MAPPER.writeValueAsString(body), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = MAPPER.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}
