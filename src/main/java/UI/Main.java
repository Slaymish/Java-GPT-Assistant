package UI;

public class Main {

    /**
     * Main function
     * @param args
     */
    public static void main(String[] args) {
        // See if the user wants to run the program in debug mode
        boolean debug = false;
        if (args.length > 0) {
            if (args[0].equals("debug")) {
                debug = true;
            }
        }

        // Run the assistant
        Assistant assistant = new Assistant();
        UI ui = new UI().setAssistant(assistant);
        ui.run(debug);
    }
}
