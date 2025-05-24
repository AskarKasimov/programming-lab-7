package ru.askar.serverLab7.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.common.object.Ticket;
import ru.askar.serverLab7.collection.CollectionManager;

import java.sql.SQLException;

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
        if (collectionManager.get(idToUpdate) == null) {
            return new CommandResponse(CommandResponseCode.ERROR, "Элемент с таким id не найден");
        }
        if (object.compareTo(collectionManager.get(idToUpdate)) < 0) {
            Ticket oldTicket = collectionManager.get(idToUpdate);
            try {
                collectionManager.remove(object.getId(), credentials);
            } catch (SQLException e) {
                return new CommandResponse(CommandResponseCode.ERROR, "Ошибка удаления старого элемента");
            }
            try {
                collectionManager.putWithValidation(object, credentials);
            } catch (InvalidInputFieldException | SQLException e) {
                try {
                    collectionManager.putWithValidation(oldTicket, credentials);
                } catch (InvalidInputFieldException | SQLException ignored) {
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
