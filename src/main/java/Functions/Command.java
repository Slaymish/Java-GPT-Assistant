package Functions;

public interface Command {

    // Execute the command
    void execute(String query);

    // Get the result of the command
    String getResult();

}