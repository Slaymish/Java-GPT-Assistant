package UI;

import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

public class UI {
    private Assistant assistant;
    private JFrame frame;
    private JPanel textPanel;
    private JPanel inputPanel;

    public void setAssistant(Assistant assistant) {
        this.assistant = assistant;
    }

    /**
     * Start the assistant.
     * This method will keep asking for input until the user types 'exit'.
     */
    public void startCLI() {
        System.out.println("Welcome to the assistant!");
        System.out.println("Type 'exit' to exit.");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            String[] parts = input.split(" ", 2);
            String command = parts[0];
            String query = parts.length > 1 ? parts[1] : "";
            String result = assistant.executeCommand(command, query);
            System.out.println(result);
        }
    }


    /**
     * Start the assistant.
     */
    public void startGUI() {
        JFrame frame = getFrame();
        frame.setLayout(new BorderLayout());
        frame.add(getTextPanel(), BorderLayout.CENTER);
        frame.add(getInputPanel(), BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public JPanel getInputPanel(){
        if(inputPanel==null) {
            inputPanel = new JPanel();
            JTextField input = new JTextField();
            input.setPreferredSize(new Dimension(300, 30));
            input.addActionListener(e -> {
                String[] parts = input.getText().split(" ", 2);
                String command = parts[0];
                String query = parts.length > 1 ? parts[1] : "";
                String result = assistant.executeCommand(command, query);

                // Clear the input and text panel
                input.setText("");
                getTextPanel().removeAll();
                getTextPanel().revalidate();

                // Add the result to the text panel
                JTextArea text = new JTextArea(result);
                text.setEditable(false);
                text.setLineWrap(true);
                text.setWrapStyleWord(true);
                getTextPanel().add(text);
                // TODO: Make reply appear correctly
            });
            inputPanel.add(input);
        }

        return inputPanel;
    }

    public JPanel getTextPanel(){
        if(textPanel==null){
            textPanel = new JPanel();
            textPanel.add(new JLabel("Welcome to the assistant!"));
        }
        return textPanel;
    }

    public JFrame getFrame(){
        if (frame == null) {
            frame = new JFrame("Assistant");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 300);
            frame.setLocationRelativeTo(null);
        }
        return frame;
    }

    /**
     * Run the assistant.
     *
     * @param gui Whether to run the assistant in GUI mode.
     */
    public void run(boolean gui) {
        if (gui) {
            startGUI();
        } else {
            startCLI();
        }
    }
}
