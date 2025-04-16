package ru.askar.serverLab6.serverCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.connection.ServerHandler;

public class ServerStatusCommand extends ServerCommand {
    public ServerStatusCommand(ServerHandler serverHandler) {
        super("status", 0, "status - вывести информацию о состоянии сервера", serverHandler);
    }

    @Override
    public CommandResponse execute(String[] args) {
        if (serverHandler.getStatus()) {
            return new CommandResponse(
                    CommandResponseCode.INFO,
                    "Сервер работает на порту " + serverHandler.getPort());
        } else {
            return new CommandResponse(CommandResponseCode.INFO, "Сервер не запущен");
        }
    }
}
