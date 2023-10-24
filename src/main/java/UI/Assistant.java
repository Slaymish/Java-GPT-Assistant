package UI;

import Functions.Command;
import Functions.CommandFactory;
import Functions.CommandNotFound;

public class Assistant {
    public String executeCommand(String command, String query) {
        Command cmd = CommandFactory.getCommand(command, query)
                        .orElse(new CommandNotFound(command, query));
        cmd.execute(query);
        return cmd.getResult();
    }
}
