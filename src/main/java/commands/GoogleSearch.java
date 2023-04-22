package commands;

// import HttpClient;

import java.io.IOException;
import java.net.http.HttpClient;

// import arraylist
import java.util.ArrayList;

// import custom search engine
import com.google.api.client.json.*;
import com.google.api.client.http.*;
import com.google.api.client.util.*;
import com.google.api.client.googleapis.*;



public class GoogleSearch {
    public static String search(String query) {
        String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
        String url = "https://www.google.com/search?q=" + encodedQuery;

        ArrayList<String> response = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(
            java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .build(),
            java.net.http.HttpResponse.BodyHandlers.ofString()
        ).thenApply(java.net.http.HttpResponse::body)
            .thenAccept(response::add)
            .join();


        System.out.println("Opening " + url);
        open(url);
        return response.toString();
    }

    private static void open(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Object google(String query) {
        return null;
    }
}
