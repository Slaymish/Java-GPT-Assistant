package Functions;

import java.util.List;
import java.util.Optional;

/**
 * Factory to create commands.
 */
public class CommandFactory {

    // List of all commands
    static List<Class<? extends Command>> commands = List.of(
            TerminalCommand.class,
            SearchSites.class,
            RelatedSearches.class,
            Help.class
    );

    /**
     * Get a instance of command by name (case insensitive).
     * If the command is not found, return an empty optional.
     *
     * @param function name of the command
     * @return
     */
    public static Optional<Command> getCommand(String function) {
        for (Class<? extends Command> command : commands) {
            if (command.getSimpleName().equalsIgnoreCase(function)) {
                try {
                    return Optional.of(command.getConstructor().newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return Optional.empty();
    }
}
