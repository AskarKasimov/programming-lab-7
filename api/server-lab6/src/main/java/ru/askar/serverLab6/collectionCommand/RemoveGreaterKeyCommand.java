package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.collection.CollectionManager;

public class RemoveGreaterKeyCommand extends CollectionCommand {
    public RemoveGreaterKeyCommand(CollectionManager collectionManager) {
        super(
                "remove_greater_key",
                1,
                "remove_greater_key key - удалить элементы, ключ которых превышает заданный",
                collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        Long key;
        try {
            key = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return new CommandResponse(CommandResponseCode.ERROR, "В поле key требуется число");
        }
        int lastSize = collectionManager.getCollection().size();
        collectionManager.getCollection().values().stream()
                .filter(t -> t.getId() > key)
                .forEach(t -> collectionManager.remove(t.getId()));
        if (lastSize == collectionManager.getCollection().size()) {
            return new CommandResponse(CommandResponseCode.ERROR, "Элементы не найдены");
        } else {
            return new CommandResponse(CommandResponseCode.SUCCESS, "Элементы удалены");
        }
    }
}
