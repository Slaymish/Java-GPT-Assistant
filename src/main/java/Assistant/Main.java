package Assistant;

import Util.Beads;
import Util.TextToSpeech;
import Util.Transcribe;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import io.github.cdimascio.dotenv.Dotenv;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    private static final ArrayList<ChatMessage> messages = new ArrayList<>();
    private static boolean running = true;



    /*  Envirorment vars:   */
    private static String model = "";
    private static String apiKey = "";
    private static String XI_API_KEY = "";
    private static String voiceID = "";
    private static String workingDirectory = "";
    private static boolean ttsEnabled = false;



    /* Command patterns */
    private static final Pattern WRITE_TO_FILE = Pattern.compile("write_to_file");
    private static final Pattern DOWNLOAD_FILE = Pattern.compile("download_file");
    private static final Pattern SELF_PROMPT = Pattern.compile("self_prompt");
    private static final Pattern READ_FILE = Pattern.compile("read_file");
    private static final Pattern READ_DIRECTORY = Pattern.compile("read_directory");
    private static final Pattern GOOGLE = Pattern.compile("google");


    public static void init(){
        Dotenv dotenv = Dotenv.load();

        // Get the value of an environment variable
        apiKey = dotenv.get("OPEN_AI_API_KEY");
        model = dotenv.get("OPEN_AI_MODEL");
        XI_API_KEY = dotenv.get("ELEVENLABS_API_KEY");
        voiceID = dotenv.get("ELEVENLABS_VOICE_ID");
        workingDirectory = dotenv.get("WORKING_DIRECTORY");
        ttsEnabled = Boolean.parseBoolean(dotenv.get("TTS_ENABLED"));


        if(apiKey == null || model == null || XI_API_KEY == null || voiceID == null || workingDirectory == null){
            throw new RuntimeException("Please set your environment variables");
        }

        System.out.println("Environment variables loaded");

    }

    public static void main(String[] args) throws IOException, LineUnavailableException {
        init();

        OpenAiService service = new OpenAiService(apiKey);

        // Load prompt from resources/prompt.txt
        String systemPrompt = new Scanner(new File("src/main/resources/prompt.txt")).useDelimiter("\\Z").next();

        resetMessages(systemPrompt);

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
    private static void runBot(OpenAiService service, String prompt, String model) throws IOException {
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
                        if(botResponse.equals("null") && !res[0].equals("")){ // finished
                            try {
                                TextToSpeech.outputTextToSpeak(res[0]); // TTS
                                messages.add(new ChatMessage("assistant", res[0]));
                                System.out.println();

                                // Check for commands
                                if(!checkForCommands(res[0],service)){
                                    runBot(service, null,model);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else if(!botResponse.equals("null")){ // chunks still coming in
                            res[0] = res[0] + botResponse;
                            System.out.print(botResponse);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    private static boolean checkForCommands(String re, OpenAiService service) throws LineUnavailableException, IOException, ParseException {
        if(WRITE_TO_FILE.matcher(re).find()){
            System.out.println("write to file");
        }
        if(DOWNLOAD_FILE.matcher(re).find()){
            System.out.println("download file");
        }
        if(READ_FILE.matcher(re).find()){
            System.out.println("read file");
        }
        if(READ_DIRECTORY.matcher(re).find()){
            System.out.println("read directory");
            messages.add(new ChatMessage("assistant", "Here are the files in your directory: " + Arrays.toString(new File(workingDirectory).list())));
        }
        else if(GOOGLE.matcher(re).find()){
            System.out.println("google");
        }
        if(SELF_PROMPT.matcher(re).find()){
            System.out.println("self prompt");
            Scanner scanner = new Scanner(re);
            scanner.useDelimiter("");
            while(scanner.hasNext()){
                String next = scanner.next();
                if(next.equals(SELF_PROMPT)){
                    requires(scanner, "{");
                    String prompt = "";
                    while(!scanner.hasNext("}")){
                        prompt += scanner.next();
                    }
                    System.out.println("Self prompt: " + prompt);
                    requires(scanner, "}");
                    runBot(service, prompt, model);
                    return true;
                }

            }
        }
        return false;
    }

    public static boolean requires(Scanner sc, String s) throws ParseException {
        if (sc.hasNext(s)) {
            sc.next(s);
            return true;
        }
        throw new ParseException("Expected " + s, 0);
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

    public static String getWorkingDirectory() {
        return workingDirectory;
    }


}

