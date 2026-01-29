package ai;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.CategoryDTO;
import model.Task;

import java.net.URI;
import java.net.http.*;

public class AIClient {

    private static final String AI_URL = "http://localhost:8080/ai/category";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public CategoryDTO suggestCategory(Task task) {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AI_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            mapper.writeValueAsString(task)
                    ))
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient()
                            .send(request, HttpResponse.BodyHandlers.ofString());

            return mapper.readValue(response.body(), CategoryDTO.class);

        } catch (Exception e) {
            throw new RuntimeException("AI category failed", e);
        }
    }

    public Task parseTask(String text) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AI_URL + "/parse"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            mapper.writeValueAsString(text)
                    ))
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient()
                            .send(request, HttpResponse.BodyHandlers.ofString());

            return mapper.readValue(response.body(), Task.class);

        } catch (Exception e) {
            throw new RuntimeException("AI parse failed", e);
        }
    }
}
