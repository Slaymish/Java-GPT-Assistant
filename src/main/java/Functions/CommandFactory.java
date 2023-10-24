package Functions;

import java.util.List;
import java.util.Optional;

/**
 * Factory to create commands.
 */
public class CommandFactory {

    // List of all commands
    private static List<Class> commands = List.of(
            TerminalCommand.class,
            Search.class
    );

    /**
     * Get a instance of command by name (case insensitive).
     * If the command is not found, return an empty optional.
     *
     * @param function name of the command
     * @param query query to be executed
     * @return
     */
    public static Optional<Command> getCommand(String function, String query) {
        for (Class command : commands) {
            if (command.getSimpleName().equalsIgnoreCase(function)) {
                try {
                    return Optional.of((Command) command.getConstructor(String.class).newInstance(query));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return Optional.empty();
    }
}
