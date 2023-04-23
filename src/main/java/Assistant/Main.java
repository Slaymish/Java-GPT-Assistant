package Assistant;

import Util.Beads;
import Util.TextToSpeech;
import Util.Transcribe;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import commands.GoogleSearch;
import io.github.cdimascio.dotenv.Dotenv;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    private static final ArrayList<ChatMessage> messages = new ArrayList<>();
    private static boolean running = true;
    private static boolean verbose = true;



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
    private static final Pattern GOOGLE_SEARCH = Pattern.compile("google_search");

    private  static final Pattern SEARCH_WEBSITE = Pattern.compile("search_website");
    private static final Pattern CURLY_BRACKETS = Pattern.compile("\\{(.+?)\\}");


    public static void init(){
        Dotenv dotenv = Dotenv.load();

        // Get the value of an environment variable
        apiKey = dotenv.get("OPEN_AI_API_KEY");
        model = dotenv.get("OPEN_AI_MODEL");
        XI_API_KEY = dotenv.get("ELEVENLABS_API_KEY");
        voiceID = dotenv.get("ELEVENLABS_VOICE_ID");
        workingDirectory = dotenv.get("WORKING_DIRECTORY");
        ttsEnabled = Boolean.parseBoolean(dotenv.get("TTS_ENABLED"));

        if (ttsEnabled) {
            System.out.println("TTS Enabled");
            if(XI_API_KEY == null || voiceID == null){
                throw new RuntimeException("Please set tts environment variables or disable tts");
            }
        } else {System.out.println("TTS Disabled");}

        if(apiKey == null || model == null || workingDirectory == null){
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
            // Still in testing
            System.out.println("\033[0;31m" + "Voice input not currently working (use text)" + "\033[0m");
            return;

            // Get user voice
            /*
            File userAudio = Beads.main();

            // Use whisper api
            prompt = Transcribe.transcribe(userAudio);
            */
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
                printMessages(messages);
                System.exit(0);
            }
            else{
                ChatCompletionRequest request = createChatCompletionRequest(messages, prompt, model);
                messages.add(new ChatMessage("user", prompt));

                try {
                    extractedStreamChat(service, model, res, request,false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    private static void printMessages(ArrayList<ChatMessage> messages) {
        for(ChatMessage message : messages){
            if(message.getRole().equals("user")){
                System.out.println("\033[0;34m" + message.getRole() + ": " + message.getContent() + "\033[0m");
            }
            else{
                System.out.println("\033[0;32m" + message.getRole() + ": " + message.getContent() + "\033[0m");
            }
        }

    }

    private static void extractedStreamChat(OpenAiService service, String model, String[] res, ChatCompletionRequest request, boolean selfMode) {
        // Create a chat completion stream
        Flowable<ChatCompletionChunk> stream = service.streamChatCompletion(request);
        // Subscribe to the stream
        stream.subscribe(chunk -> {
            // Get the latest message
            ChatCompletionChoice result = chunk.getChoices().get(0);
            String botResponse = String.valueOf(result.getMessage().getContent());
            if(botResponse.equals("null") && !res[0].equals("")){ // finished
                try {
                    if(ttsEnabled){TextToSpeech.outputTextToSpeak(res[0]);} // TTS
                    messages.add(new ChatMessage("assistant", res[0]));
                    System.out.println("\n");
                    // Check for commands
                    if(!checkForCommands(res[0], service) || selfMode){
                        runBot(service, null, model);
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
    }

    private static boolean checkForCommands(String re, OpenAiService service) throws Exception {
        if(WRITE_TO_FILE.matcher(re).find()){
            parseWriteFile(re);
        }
        if(DOWNLOAD_FILE.matcher(re).find()){
            System.out.println("download file");
        }
        if(READ_FILE.matcher(re).find()){
            parseReadFile(re);
        }
        if(READ_DIRECTORY.matcher(re).find()){
            System.out.println("read directory");
            messages.add(new ChatMessage("assistant", "Here are the files in your directory: " + Arrays.toString(new File(workingDirectory).list())));
        }
        if(GOOGLE_SEARCH.matcher(re).find()){
            parseGoogle(re);
        }
        if(SEARCH_WEBSITE.matcher(re).find()){
            System.out.println("search website");
        }
        else
        if(SELF_PROMPT.matcher(re).find()){
            standAlonePrompt(service, re, model);
        }
        return false;
    }

    private static void parseGoogle(String sc) {
        // Get the search query
        String query = sc.substring(sc.indexOf("google_search") + ("google_search").length() + 1);
        query = query.substring(0, query.indexOf("self_prompt") - 2);
        System.out.println("searching for: " + query);

        String searchResults = GoogleSearch.search(query);

        messages.add(new ChatMessage("assistant", "Here are the results for " + query + ": " + searchResults));
    }

    private static void parseWriteFile(String sc) {
        // Get the search query
        String query = sc.substring(sc.indexOf("write_to_file") + ("write_to_file").length());
        query = query.substring(0, query.indexOf("self_prompt") - 2);

        // Get the file name
        String fileName = query.substring(1,query.indexOf(","));
        System.out.println("file name: " + fileName);

        // Get the file contents
        String fileContents = query.substring(query.indexOf(",") + 1);
        System.out.println("file contents: " + fileContents);

        // Write to file
        try{
            FileWriter myWriter = new FileWriter(workingDirectory + fileName);
            myWriter.write(fileContents);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");

        }
        catch(Exception e){
            System.out.println("Error: " + e);
        }
    }

    private static void parseReadFile(String sc) {
        // Get the search query
        String query = sc.substring(sc.indexOf("read_file") + ("read_file").length() + 1);
        query = query.substring(0, query.indexOf("self_prompt") - 2);
        System.out.println("reading file: " + query);

        // Read file
        try {
            File myObj = new File(workingDirectory + query);
            String data = "";
            if(myObj.exists()) {
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    data = data + "\n" + myReader.nextLine();
                }
                myReader.close();
                messages.add(new ChatMessage("assistant", "Here is the contents of the file: " + data));
            }
            else{
                System.out.println("File does not exist.");
                messages.add(new ChatMessage("assistant", "File does not exist."));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void standAlonePrompt(OpenAiService service, String prompt, String model) {
        // Construct prompt
        String initalPrompt = "The following is prompted by you to query the information in the previous messages. Try to answer the query as concisely and correctly as possible:\n\n";
        prompt = initalPrompt + prompt;

        // Create a chat completion request
        ChatCompletionRequest request = createChatCompletionRequest(messages, prompt, model);
        messages.add(new ChatMessage("user", prompt));

        // Create a chat completion stream
        extractedStreamChat(service, model, new String[]{""}, request,true);
    }

    public static boolean requires(Scanner sc, String s) throws Exception {
        try{
            if (sc.hasNext(s)) {
                if(verbose){System.out.println("Found " + s);}
                sc.next(s);
                return true;
            }
        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        throw new Exception("Parse Error, expected " + s + " but found " + sc.next());
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

