package org.newsrec.crawler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SemanticScholarCrawler {

    private static final Logger logger = LogManager.getLogger(SemanticScholarCrawler.class);
    private static final String API_URL = "https://api.semanticscholar.org/graph/v1/paper/search";
    private static final int LIMIT = 10;

    private final HttpClient client;
    private final Gson gson;

    public SemanticScholarCrawler() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public List<RSSArticle> search(List<String> keywords) {
        List<RSSArticle> articles = new ArrayList<>();
        for (String keyword : keywords) {
            articles.addAll(search(keyword));
        }
        return articles;
    }

    public List<RSSArticle> search(String keyword) {
        List<RSSArticle> articles = new ArrayList<>();
        try {
            String query = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = API_URL + "?query=" + query + "&limit=" + LIMIT + "&fields=title,url,abstract";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Semantic Scholar API returned status: {} for keyword: {}", response.statusCode(), keyword);
                return articles;
            }

            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            JsonArray data = json.getAsJsonArray("data");

            if (data == null) return articles;

            for (int i = 0; i < data.size(); i++) {
                JsonObject paper = data.get(i).getAsJsonObject();
                String title = getString(paper, "title");
                String link = getString(paper, "url");
                String description = getString(paper, "abstract");

                if (title == null || title.isEmpty()) continue;
                if (description == null) description = "";
                if (link == null || link.isEmpty()) {
                    link = "https://semanticscholar.org/paper/" + getString(paper, "paperId");
                }

                articles.add(new RSSArticle(title, link, description, "SemanticScholar"));
            }

        } catch (Exception e) {
            logger.error("Failed to fetch Semantic Scholar for keyword: {}", keyword, e);
        }

        return articles;
    }

    private String getString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return null;
    }
}
