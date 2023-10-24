package UI;

public class Main {
    public static void main(String[] args) {
        UI ui = new UI();
        Assistant assistant = new Assistant();
        ui.setAssistant(assistant);
        ui.run(true);
    }
}
