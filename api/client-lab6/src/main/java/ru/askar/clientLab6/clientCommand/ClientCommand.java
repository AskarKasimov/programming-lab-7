package ru.askar.clientLab6.clientCommand;

import ru.askar.clientLab6.connection.ClientHandler;
import ru.askar.common.cli.Command;

public abstract class ClientCommand extends Command {
    protected final ClientHandler clientHandler;

    /**
     * Заполнение имени и количества требуемых аргументов
     *
     * @param name
     * @param argsCount
     */
    public ClientCommand(String name, int argsCount, String info, ClientHandler clientHandler) {
        super(name, argsCount, info);
        this.clientHandler = clientHandler;
    }
}
