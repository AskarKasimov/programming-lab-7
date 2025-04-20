package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.serverLab6.collection.CollectionManager;

public class InsertCommand extends ObjectCollectionCommand {
    public InsertCommand(CollectionManager collectionManager) {
        super("insert", 3, "insert id?null name price - добавить новый элемент", collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        if (object == null)
            return new CommandResponse(
                    CommandResponseCode.ERROR, "Данной команде требуется объект!");
        try {
            collectionManager.putWithValidation(object);
        } catch (InvalidInputFieldException e) {
            return new CommandResponse(CommandResponseCode.ERROR, e.getMessage());
        }
        return new CommandResponse(CommandResponseCode.SUCCESS, "Элемент добавлен");
    }
}
