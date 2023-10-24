package Functions;

import java.io.IOException;
import java.lang.Process;

public class TerminalCommand implements Command {
    private String command;
    private String[] results;

    public TerminalCommand(String query) {
        this.command = query;
    }

    public void execute(String query) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(query);
            Process process = builder.start();
            process.waitFor();
            results = process.getInputStream().readAllBytes().toString().split("\n");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getResult() {
        return String.join("\n", results);
    }

}
