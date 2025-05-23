package ru.askar.serverLab7.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab7.collection.CollectionManager;

public class InfoCommand extends CollectionCommand {
    public InfoCommand(CollectionManager collectionManager) {
        super(
                "info",
                0,
                "info - вывести информацию о коллекции (тип, дата инициализации, количество элементов)",
                collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        String info =
                "Дата инициализации: "
                        + collectionManager.getDateOfCreation()
                        + "\nКоличество элементов: "
                        + collectionManager.getCollectionValuesStream().count();
        return new CommandResponse(CommandResponseCode.INFO, info);
    }
}
