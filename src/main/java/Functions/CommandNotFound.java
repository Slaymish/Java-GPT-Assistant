package Functions;

public class CommandNotFound implements Command {
    String query;
    String command;

    public CommandNotFound(String command, String query) {
        this.command = command;
        this.query = query;
    }

    @Override
    public void execute(String query) {}

    @Override
    public String getResult() {
        return "Command not found: " + command;
    }
}
