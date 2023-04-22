package Util;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TextToSpeech {
    private static final int CHUNK_SIZE = 1024;
    private static final String URL_STRING = "https://api.elevenlabs.io/v1/text-to-speech/21m00Tcm4TlvDq8ikWAM";
    private static String XI_API_KEY = "";


    public static void outputTextToSpeak(String args) throws IOException {
        Dotenv dotenv = Dotenv.load();
        XI_API_KEY = dotenv.get("ELEVENLABS_API_KEY");
        String voiceID = dotenv.get("ELEVENLABS_VOICE_ID");

        URL url = new URL(URL_STRING);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "audio/mpeg");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("xi-api-key", XI_API_KEY);
        connection.setDoOutput(true);

        String text = args;

        // santize the text
        String encodedText = text.replaceAll("\n", ". ")
                .replaceAll("\r", " ")
                .replaceAll("\t", " ")
                .replaceAll("\\\"","`");


        // Build the body
        String body = String.format("{\"text\": \"%s\", \"voice_settings\": {\"stability\": 0, \"similarity_boost\": 0}}", encodedText);

        OutputStream os = connection.getOutputStream();
        byte[] input = body.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (OutputStream outputStream = new FileOutputStream("output.mp3")) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = connection.getInputStream().read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Speak the output
                Runtime.getRuntime().exec("afplay output.mp3");
            }
        } else {
            System.out.println("Error: " + responseCode);

            // Display issue
            System.out.println("Error: " + connection.getResponseMessage());

            // Speak the error
            System.out.println("text: " + text);
            System.out.println("encodedText: " + encodedText);
        }
    }
}
