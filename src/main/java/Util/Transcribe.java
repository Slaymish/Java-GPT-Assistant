package Util;

import com.theokanning.openai.service.OpenAiService;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import com.theokanning.openai.engine.*;
import com.theokanning.openai.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.sound.sampled.*;


public class Transcribe {
    public static String transcribe(File audioData) throws IOException {
        Dotenv dotenv = Dotenv.load();

        // Get the value of an environment variable
        String apiKey = dotenv.get("OPEN_AI_API_KEY");


        // Set the URL endpoint and token for the OpenAI API
        String endpoint = "https://api.openai.com/v1/audio/translations";
        String token = "TOKEN"; // Replace with your actual token

        // Set the model name for the audio translation
        String model = "whisper-1";

        // Create a CloseableHttpClient object to send the HTTP request
        CloseableHttpClient client = HttpClients.createDefault();

        // Create a HttpPost object with the necessary headers and form data
        HttpPost request = new HttpPost(endpoint);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", audioData, ContentType.DEFAULT_BINARY, audioData.getName())
                .addTextBody("model", model)
                .build();
        request.setEntity(entity);

        // Send the HttpRequest asynchronously and print the response body
        HttpResponse response = (HttpResponse) client.execute(request);

        System.out.println((response.body().toString()));

        return response.body().toString();
    }
}
