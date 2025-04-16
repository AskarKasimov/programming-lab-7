package ru.askar.serverLab6.serverCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.exception.ExitCLIException;
import ru.askar.serverLab6.connection.ServerHandler;

public class ServerExitCommand extends ServerCommand {
    public ServerExitCommand(ServerHandler serverHandler) {
        super("exit", 0, "exit - завершить программу", serverHandler);
    }

    @Override
    public CommandResponse execute(String[] args) throws ExitCLIException {
        new ServerStopCommand(serverHandler).execute(new String[] {});
        throw new ExitCLIException();
    }
}
