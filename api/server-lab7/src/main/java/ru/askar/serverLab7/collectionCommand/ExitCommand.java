package ru.askar.serverLab7.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.serverLab7.ClientDisconnectException;

public class ExitCommand extends CollectionCommand {
    public ExitCommand() {
        super("exit", 0, "exit - завершить программу (без сохранения в файл)", null);
    }

    @Override
    public CommandResponse execute(String[] args) {
        throw new ClientDisconnectException();
    }
}
