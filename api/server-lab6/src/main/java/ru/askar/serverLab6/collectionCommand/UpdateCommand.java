package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.serverLab6.collection.CollectionManager;

import java.sql.SQLException;

public class UpdateCommand extends ObjectCollectionCommand {
    public UpdateCommand(CollectionManager collectionManager) {
        super(
                "update",
                3,
                "update id name price - обновить значение элемента коллекции, id которого равен заданному",
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
        if (object.getId() == null) {
            return new CommandResponse(
                    CommandResponseCode.ERROR, "id элемента в объекте не может быть null");
        }
        if (!idToUpdate.equals(object.getId())) {
            return new CommandResponse(
                    CommandResponseCode.ERROR,
                    "id элемента из аргумента не совпадает с id объекта");
        }
        if (collectionManager.get(idToUpdate) == null) {
            return new CommandResponse(CommandResponseCode.ERROR, "Элемент с таким id не найден");
        }
        try {
            collectionManager.remove(idToUpdate, credentials);
        } catch (SQLException e) {
            return new CommandResponse(CommandResponseCode.ERROR, e.getMessage());
        }
        try {
            collectionManager.putWithValidation(object);
        } catch (InvalidInputFieldException | SQLException e) {
            return new CommandResponse(CommandResponseCode.ERROR, e.getMessage());
        }
        return new CommandResponse(CommandResponseCode.SUCCESS, "Элемент заменён");
    }
}
