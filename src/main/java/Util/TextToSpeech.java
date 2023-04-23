package Util;

import io.github.cdimascio.dotenv.Dotenv;

import javax.sound.sampled.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TextToSpeech {
    private static final int CHUNK_SIZE = 1024;
    private static final String URL_STRING = "https://api.elevenlabs.io/v1/text-to-speech/";
    private static String XI_API_KEY = "";


    public static void outputTextToSpeak(String fullResponse) throws IOException, LineUnavailableException, UnsupportedAudioFileException, InterruptedException {
        Dotenv dotenv = Dotenv.load();
        XI_API_KEY = dotenv.get("ELEVENLABS_API_KEY");
        String voiceID = dotenv.get("ELEVENLABS_VOICE_ID");
        int stability = 0;
        int similarityBoost = 0;

        // Get the first line of the response
        String text = fullResponse.split("\n")[0];

        // Get substring
        String toSpeak = text.substring(("Response:").length(), Math.min(text.length(), 1000));


        // santize the text
        String encodedText = toSpeak.replaceAll("\n", ". ")
                .replaceAll("\r", " ")
                .replaceAll("\t", " ")
                .replaceAll("\\\"","'");


        URL url = new URL(URL_STRING + voiceID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("accept", "audio/mpeg");
        conn.setRequestProperty("xi-api-key", XI_API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = "{ \"text\": \"" + encodedText + "\", \"voice_settings\": { \"stability\": " + stability + ", \"similarity_boost\": " + similarityBoost + " } }";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Get the response
        byte[] buffer = new byte[CHUNK_SIZE];
        int bytesRead;
        try (InputStream is = conn.getInputStream()) {
            try (FileOutputStream fos = new FileOutputStream("output.mp3")) {
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        }
        catch (IOException e) {
            System.out.println("");
            System.out.println("\033[0;31m" + "Error with TTS: Check you have enough credits on your account"+ "\033[0m");

            // Print to console in red
            System.out.println("\033[0;31m" + "Error: " + e + "\033[0m");

            // Print to console in blue
            System.out.println("\033[0;34m" + "Failed to speak: " + encodedText + "\033[0m");
        }

        // Play the audio
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("output.mp3"));
    }

}
