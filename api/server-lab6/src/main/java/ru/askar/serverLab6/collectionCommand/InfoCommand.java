package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.collection.CollectionManager;

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
                "Тип коллекции: "
                        + collectionManager.getCollection().getClass()
                        + "\nДата инициализации: "
                        + collectionManager.getDateOfCreation()
                        + "\nКоличество элементов: "
                        + collectionManager.getCollection().size();
        return new CommandResponse(CommandResponseCode.INFO, info);
    }
}
