package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.common.object.Ticket;
import ru.askar.serverLab6.collection.CollectionManager;

public class ReplaceIfGreaterCommand extends ObjectCollectionCommand {
    public ReplaceIfGreaterCommand(CollectionManager collectionManager) {
        super(
                "replace_if_greater",
                3,
                "replace_if_greater id name price - заменить значение по ключу, если новое значение больше старого",
                collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        if (object == null)
            return new CommandResponse(
                    CommandResponseCode.ERROR, "Данной команде требуется объект!");
        Long idToUpdate;
        try {
            idToUpdate = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return new CommandResponse(CommandResponseCode.ERROR, "В поле id требуется число");
        }
        if (!idToUpdate.equals(object.getId())) {
            return new CommandResponse(
                    CommandResponseCode.ERROR,
                    "id элемента из аргумента не совпадает с id объекта");
        }
        if (collectionManager.getCollection().get(idToUpdate) == null) {
            return new CommandResponse(CommandResponseCode.ERROR, "Элемент с таким id не найден");
        }
        if (object.compareTo(collectionManager.getCollection().get(idToUpdate)) < 0) {
            Ticket oldTicket = collectionManager.getCollection().get(idToUpdate);
            try {
                collectionManager.remove(object.getId());
                collectionManager.putWithValidation(object);
            } catch (InvalidInputFieldException e) {
                try {
                    collectionManager.putWithValidation(oldTicket);
                } catch (InvalidInputFieldException ignored) {
                    // такого не может быть:))
                }
                return new CommandResponse(CommandResponseCode.ERROR, e.getMessage());
            }
        } else {
            return new CommandResponse(
                    CommandResponseCode.WARNING,
                    "Новое значение не больше старого. Коллекция осталась прежней");
        }
        return new CommandResponse(
                CommandResponseCode.SUCCESS, "Элемент успешно заменён на больший");
    }
}
