package ru.askar.clientLab6.clientCommand;

import ru.askar.clientLab6.NeedToReconnectException;
import ru.askar.clientLab6.connection.ClientHandler;
import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;

import java.io.IOException;

public class ClientStartCommand extends ClientCommand {
    private static final int TIME_FOR_RETRY = 2000;

    public ClientStartCommand(ClientHandler clientHandler) {
        super(
                "start",
                2,
                "start host port - запуск клиента на указанный хост и порт",
                clientHandler);
    }

    @Override
    public CommandResponse execute(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        if (clientHandler.getRunning()) {
            return new CommandResponse(CommandResponseCode.ERROR, "Клиент уже запущен!");
        } else {
            clientHandler.setHost(host);
            clientHandler.setPort(port);
            Thread handlerThread =
                    new Thread(
                            () -> {
                                try {
                                    clientHandler.start();
                                } catch (IOException e) {
                                    System.out.println(
                                            "Произошла ошибка на клиенте: " + e.getMessage());
                                } catch (NeedToReconnectException e) {
                                    System.out.println("Переподключаюсь");
                                    try {
                                        Thread.sleep(TIME_FOR_RETRY);
                                    } catch (InterruptedException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                    execute(args);
                                }
                            });
            handlerThread.start();

            return new CommandResponse(
                    CommandResponseCode.INFO, "Пытаюсь подключиться к " + host + ":" + port);
        }
    }
}
