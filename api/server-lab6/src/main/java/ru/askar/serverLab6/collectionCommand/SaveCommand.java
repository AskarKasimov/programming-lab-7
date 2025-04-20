package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.collection.CollectionManager;

public class SaveCommand extends CollectionCommand {
    public SaveCommand(CollectionManager collectionManager) {
        super("save", 0, "save - сохранить коллекцию в файл", collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        return new CommandResponse(CommandResponseCode.ERROR, "Команда не поддерживается");
    }
}
