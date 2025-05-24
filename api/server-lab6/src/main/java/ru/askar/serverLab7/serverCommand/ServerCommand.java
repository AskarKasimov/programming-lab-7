package ru.askar.serverLab7.serverCommand;

import ru.askar.common.cli.Command;
import ru.askar.serverLab7.connection.ServerHandler;

public abstract class ServerCommand extends Command {
    protected final ServerHandler serverHandler;

    /**
     * Заполнение имени и количества требуемых аргументов
     *
     * @param name
     * @param argsCount
     */
    public ServerCommand(String name, int argsCount, String info, ServerHandler serverHandler) {
        super(name, argsCount, info);
        this.serverHandler = serverHandler;
    }
}
