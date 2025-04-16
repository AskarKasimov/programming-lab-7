package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;

public class ExitCommand extends CollectionCommand {
    public ExitCommand() {
        super("exit", 0, "exit - завершить программу (без сохранения в файл)", null);
    }

    @Override
    public CommandResponse execute(String[] args) {
        return null; // отлов вызова этой команды в ServerHandler
    }
}
