package in.codemonks.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void post(String urlStr, Map<String, Object> body) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(MAPPER.writeValueAsBytes(body));
        }

        int code = conn.getResponseCode();
        if (code >= 400) throw new RuntimeException("HTTP POST failed with code " + code);
    }

    public static Map<?, ?> postJson(String urlStr, Map<String, Object> body) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(MAPPER.writeValueAsBytes(body));
        }

        return MAPPER.readValue(conn.getInputStream(), Map.class);
    }
}
