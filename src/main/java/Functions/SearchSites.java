package Functions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchSites implements Command {
    private final List<Result> results;

    public SearchSites() {
        this.results = new ArrayList<>();
    }

    public void execute(String query) {
        results.addAll(getWebsiteResults(query));
    }

    public String getResult() {
        return results.stream()
                .map(Result::toString)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("No results found");
    }

    private List<Result> getWebsiteResults(String query) {
        List<Result> resultLinks = new ArrayList<>();
        try {
            // Fetch the HTML content
            Document doc = Jsoup.connect("https://duckduckgo.com/html/?q=" + query).get();

            // Parse and find the links
            Elements links = doc.select("div[class*=links_main] a");

            int i = 0;
            for (Element link : links) {
                String href = link.attr("href");
                if (resultLinks.stream().anyMatch(r -> r.getUrl().toString().equals(href))
                || i++ > 5) {
                    continue;
                }
                resultLinks.add(new Result(href, link.text()));
            }

        } catch (IOException e) {
            // Handle exceptions
            System.out.println("Error: " + e.getMessage());
        }

        return resultLinks;
    }
}
