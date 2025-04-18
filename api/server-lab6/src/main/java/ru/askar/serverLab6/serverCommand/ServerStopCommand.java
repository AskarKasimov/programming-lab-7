package ru.askar.serverLab6.serverCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.connection.ServerHandler;

public class ServerStopCommand extends ServerCommand {
    public ServerStopCommand(ServerHandler serverHandler) {
        super("stop", 0, "stop - остановить сервер", serverHandler);
    }

    @Override
    public CommandResponse execute(String[] args) {
        if (serverHandler.getStatus()) {
            serverHandler.stop();
        } else {
            return new CommandResponse(CommandResponseCode.ERROR, "Сервер не запущен");
        }
        return new CommandResponse(CommandResponseCode.SUCCESS, "Сервер остановлен");
    }
}
