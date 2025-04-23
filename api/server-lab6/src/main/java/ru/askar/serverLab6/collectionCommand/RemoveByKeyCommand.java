package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.collection.CollectionManager;

public class RemoveByKeyCommand extends CollectionCommand {
    public RemoveByKeyCommand(CollectionManager collectionManager) {
        super(
                "remove_key",
                1,
                "remove_key key - удалить элемент из коллекции по его id",
                collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return new CommandResponse(CommandResponseCode.ERROR, "В поле id требуется число");
        }
        if (collectionManager.get(id) == null) {
            return new CommandResponse(CommandResponseCode.ERROR, "Элемент с таким id не найден");
        }
        collectionManager.remove(id);
        return new CommandResponse(CommandResponseCode.SUCCESS, "Элемент удалён");
    }
}
