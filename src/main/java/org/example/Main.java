package org.example;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import io.github.cdimascio.dotenv.Dotenv;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final ArrayList<ChatMessage> messages = new ArrayList<>();
    private static boolean running = true;

    private static String model = "";

    public static void main(String[] args) throws IOException, LineUnavailableException {
        Dotenv dotenv = Dotenv.load();

        // Get the value of an environment variable
        String apiKey = dotenv.get("OPEN_AI_API_KEY");
        model = dotenv.get("OPEN_AI_MODEL");

        OpenAiService service = new OpenAiService(apiKey);

        resetMessages("You are a helpful assistant, specialising in short responses.");

        // Ask for text or voice
        System.out.println("Would you like to use text or voice? (t/v)");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String prompt = "";

        if(input.equals("v")){
            // Get user voice
            File userAudio = Beads.main();

            // Use whisper api
            prompt = Transcribe.transcribe(userAudio);
        }
        else if(input.equals("t")){
            // Get user text
            prompt = getUser();
        }
        runBot(service, prompt,model);
    }

    /**
     * Run the bot loop
     * @param service
     * @param prompt
     * @throws LineUnavailableException
     * @throws IOException
     */
    private static void runBot(OpenAiService service, String prompt, String model) throws LineUnavailableException, IOException {
            if(prompt == null){prompt = getUser();}
            final String[] res = {""};
            if(prompt.equals("q")){
                running = false;
                System.out.println("exiting: " + messages.size() + " messages");
                messages.forEach(System.out::println);
                System.exit(0);
            }
            else{
                ChatCompletionRequest request = createChatCompletionRequest(messages, prompt, model);
                messages.add(new ChatMessage("user", prompt));

                try {
                    // Create a chat completion stream
                    Flowable<ChatCompletionChunk> stream = service.streamChatCompletion(request);
                    // Subscribe to the stream
                    stream.subscribe(chunk -> {
                        // Get the latest message
                        ChatCompletionChoice result = chunk.getChoices().get(0);
                        String botResponse = String.valueOf(result.getMessage().getContent());
                        if(botResponse.equals("null") && !res[0].equals("")){
                            try {
                                TextToSpeech.outputTextToSpeak(res[0]);
                                messages.add(new ChatMessage("assistant", res[0]));
                                System.out.println();
                                // Rerun the bot
                                runBot(service, null,model);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else if(!botResponse.equals("null")){
                            res[0] = res[0] + botResponse;
                            System.out.print(botResponse);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    /**
     * Reset the bot to the system message
      * @param system
     */
    private static void resetMessages(String system) {
        messages.clear();
        messages.add(new ChatMessage("system", system));
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

