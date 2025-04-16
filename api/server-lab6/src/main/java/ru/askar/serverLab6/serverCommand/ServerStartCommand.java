package ru.askar.serverLab6.serverCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.connection.ServerHandler;

public class ServerStartCommand extends ServerCommand {
    /** Заполнение имени и количества требуемых аргументов */
    public ServerStartCommand(ServerHandler serverHandler) {
        super("start", 1, "start port - запуск сервера на указанном порту", serverHandler);
    }

    @Override
    public CommandResponse execute(String[] args) {
        int port = Integer.parseInt(args[0]);

        if (serverHandler.getStatus()) {
            return new CommandResponse(
                    CommandResponseCode.WARNING,
                    "Сервер уже запущен на порту " + serverHandler.getPort());
        } else {
            serverHandler.setPort(port);
            Thread handlerThread =
                    new Thread(
                            () -> {
                                try {
                                    serverHandler.start();
                                } catch (Exception e) {
                                    System.out.println(
                                            "Ошибка при запуске сервера: " + e.getMessage());
                                }
                            });
            handlerThread.start();
            return new CommandResponse(
                    CommandResponseCode.SUCCESS,
                    "Сервер запускается на порту " + serverHandler.getPort());
        }
    }
}
