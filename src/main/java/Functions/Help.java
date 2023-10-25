package Functions;

public class Help implements Command {

    public Help(){}
    @Override
    public void execute(String query) {}

    @Override
    public String getResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available commands:\n");
        CommandFactory.commands.forEach(command -> {
            sb.append(command.getSimpleName()).append("\n");
        });
        return sb.toString();
    }
}
