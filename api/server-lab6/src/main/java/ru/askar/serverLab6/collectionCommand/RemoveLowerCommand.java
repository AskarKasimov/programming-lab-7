package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.serverLab6.collection.CollectionManager;

import static ru.askar.serverLab6.collection.CollectionManager.validateTicket;

public class RemoveLowerCommand extends ObjectCollectionCommand {
    public RemoveLowerCommand(CollectionManager collectionManager) {
        super(
                "remove_lower",
                2,
                "remove_lower name price - удалить из коллекции все элементы, меньшие, чем заданный",
                collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        if (object == null)
            return new CommandResponse(
                    CommandResponseCode.ERROR, "Данной команде требуется объект!");
        try {
            validateTicket(object);
        } catch (InvalidInputFieldException e) {
            return new CommandResponse(CommandResponseCode.ERROR, e.getMessage());
        }
        int lastSize = collectionManager.getCollection().size();
        collectionManager.getCollection().values().stream()
                .filter(t -> t.compareTo(object) > 0)
                .forEach(
                        t -> {
                            collectionManager.remove(t.getId());
                        });
        if (lastSize == collectionManager.getCollection().size()) {
            return new CommandResponse(CommandResponseCode.ERROR, "Элементы не найдены");
        } else {
            return new CommandResponse(CommandResponseCode.SUCCESS, "Элементы удалены");
        }
    }
}
