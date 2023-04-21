package org.example;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import io.github.cdimascio.dotenv.Dotenv;
import io.reactivex.Flowable;

import java.util.*;

public class Main {
    private static final ArrayList<ChatMessage> messages = new ArrayList<>();
    private static boolean running = true;

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        // Get the value of an environment variable
        String apiKey = dotenv.get("OPEN_AI_API_KEY");

        OpenAiService service = new OpenAiService(apiKey);

        // Add system messages
        messages.add(new ChatMessage("system", "You are a snarky bot that is arogant about how much you know. You specialise in short responses and limit all responses to at most 2 paragraphs"));

        while(running){
            String userInput = getUser();
            if(userInput.equals("q")){
                running = false;
            }
            else{
                ChatCompletionRequest request = createChatCompletionRequest(messages, userInput, "gpt-4");
                messages.add(new ChatMessage("user", userInput));

                try {
                    // Create a chat completion stream
                    Flowable<ChatCompletionChunk> stream = service.streamChatCompletion(request);
                    System.out.println("Bot: ");
                    // Subscribe to the stream
                    stream.subscribe(chunk -> {
                        // Get the latest message
                        ChatCompletionChoice result = chunk.getChoices().get(0);
                        String botResponse = String.valueOf(result.getMessage().getContent());
                        messages.add(new ChatMessage("assistant", botResponse));
                        if(!botResponse.equals(null)) {
                            System.out.print(botResponse);
                        }

                    }).isDisposed();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("exiting: " + messages.size() + " messages");


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

