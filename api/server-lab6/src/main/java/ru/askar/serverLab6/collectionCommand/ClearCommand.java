package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.collection.CollectionManager;

public class ClearCommand extends CollectionCommand {
    public ClearCommand(CollectionManager collectionManager) {
        super("clear", 0, "clear - очистить коллекцию", collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        synchronized (collectionManager) {
            if (collectionManager.getCollectionValuesStream().findAny().isEmpty())
                return new CommandResponse(CommandResponseCode.WARNING, "Коллекция пуста");
            else {
                collectionManager.clear();
                return new CommandResponse(CommandResponseCode.SUCCESS, "Коллекция очищена");
            }
        }
    }
}
