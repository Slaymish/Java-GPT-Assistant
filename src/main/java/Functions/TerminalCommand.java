package Functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Process;
import java.util.ArrayList;
import java.util.List;

public class TerminalCommand implements Command {
    private List<String> results;
    public TerminalCommand() {
        this.results = new ArrayList<>();
    }

    public void execute(String query) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("bash", "-c", query);
        int exitCode = -1;
        try {
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(line);
            }
            exitCode = process.waitFor();
            if (results.isEmpty()) {
                results.add("Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getResult() {
        return String.join("\n", results);
    }

}
