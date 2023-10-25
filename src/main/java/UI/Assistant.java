package UI;

import Functions.Command;
import Functions.CommandFactory;
import Functions.CommandNotFound;

public class Assistant {

    /**
     * Execute a command
     *
     * @param command Name of the command
     * @param query  Query to be executed
     * @return
     */
    public String executeCommand(String command, String query) {
        Command cmd = CommandFactory.getCommand(command)
                        .orElse(new CommandNotFound(command, query));
        cmd.execute(query);
        return cmd.getResult();
    }
}
