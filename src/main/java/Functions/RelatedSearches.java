package Functions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class RelatedSearches implements Command{
    private static final String BASE_URL = "https://duckduckgo.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    private final List<Result> results;

    public RelatedSearches() {
        this.results = new ArrayList<>();
    }

    public void execute(String query) {
        getInstantAnswer(query);
    }

    public String getResult() {
        return results.stream()
                .map(Result::toString)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("No results found");
    }


    /**
     * Get instant answer from DuckDuckGo API
     *
     * @param query the query to search
     */
    private void getInstantAnswer(String query) {
        String url = BASE_URL + "/?q=" + URLEncoder.encode(query) + "&format=json";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                results.addAll(getRelatedSearches(response.body()));
            } else {
                results.add(new Result(url, "Error: " + response.statusCode()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse the JSON response from DuckDuckGo API
     * and get the related searches
     * @param jsonString the JSON response
     * @return
     */
    private List<Result> getRelatedSearches(String jsonString) {
        List<Result> resultList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray relatedTopics = jsonObject.getJSONArray("RelatedTopics");
            for (int i = 0; i < relatedTopics.length(); i++) {
                JSONObject relatedTopic = relatedTopics.getJSONObject(i);
                if (relatedTopic.has("Result")) {
                    String result = relatedTopic.getString("Result");
                    String url = result.substring(result.indexOf("<a href=\"") + 9, result.indexOf("\">"));
                    String description = result.substring(result.indexOf("\">") + 2, result.indexOf("</a>"));
                    resultList.add(new Result(url, description));
                }
            }
        } catch (Exception e) {
            resultList.add(new Result(BASE_URL, "Error: " + e.getMessage()));
        }
        return resultList;
    }
}