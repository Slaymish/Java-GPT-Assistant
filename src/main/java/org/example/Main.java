package org.example;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import io.github.cdimascio.dotenv.Dotenv;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final ArrayList<ChatMessage> messages = new ArrayList<>();
    private static boolean running = true;

    public static void main(String[] args) throws IOException, LineUnavailableException {
        Dotenv dotenv = Dotenv.load();

        // Get the value of an environment variable
        String apiKey = dotenv.get("OPEN_AI_API_KEY");

        OpenAiService service = new OpenAiService(apiKey);

        // Add system messages
        messages.add(new ChatMessage("system", "You are a helpful assistant, specialising in short responses."));

        // Use whisper api
        Transcribe.transcribe();


        while(running){
            String userInput = getUser();
            final String[] res = {""};
            if(userInput.equals("q")){
                running = false;
            }
            else{
                ChatCompletionRequest request = createChatCompletionRequest(messages, userInput, "gpt-4");
                messages.add(new ChatMessage("user", userInput));

                try {
                    // Create a chat completion stream
                    System.out.println(request.getStop());
                    Flowable<ChatCompletionChunk> stream = service.streamChatCompletion(request);
                    // Subscribe to the stream
                    stream.subscribe(chunk -> {
                        // Get the latest message
                        ChatCompletionChoice result = chunk.getChoices().get(0);
                        String botResponse = String.valueOf(result.getMessage().getContent());
                        messages.add(new ChatMessage("assistant", botResponse));
                        if(botResponse.equals("null") && !res[0].equals("")){
                            //tts
                            try {
                                TextToSpeech.outputTextToSpeak(res[0]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else if(!botResponse.equals("null")){
                            res[0] = res[0] + " " + botResponse;
                            System.out.print(botResponse);
                        }

                    });








                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("exiting: " + messages.size() + " messages");


    }

    private static void executeResult(Single<List<ChatCompletionChunk>> toList) {
        try {
            List<ChatCompletionChunk> result = toList.blockingGet();
            System.out.println("result: " + result.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String getUser(List<ChatMessage> msgs) {
        return msgs.stream()
                .filter(x -> x.getRole().equals("user"))
                .map(x -> x.getContent())
                .reduce("", (x, y) -> x + "\n" + y);
    }

    private static String getBot(List<ChatMessage> msgs) {
        return msgs.stream()
                .filter(x -> x.getRole().equals("bot"))
                .map(x -> x.getContent())
                .reduce("", (x, y) -> x + "\n" + y);
    }

    private static CompletionRequest createCompletionRequest(String prompt, String model) {
        return CompletionRequest.builder()
                .prompt(prompt)
                .model(model)
                .echo(true)
                .build();
    }


    private static ChatCompletionRequest createChatCompletionRequest(List<ChatMessage>  ff, String prompt, String model) {
        return ChatCompletionRequest.builder()
                .model(model)
                .messages(ff)
                .user(prompt)
                .build();
    }

    private static String getUser(){
        // Get user input
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your message: (q to quit)  ");
        String userInput = scanner.nextLine();
        return userInput;
    }


}

