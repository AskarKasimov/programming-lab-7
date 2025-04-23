package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.exception.ExitCLIException;
import ru.askar.serverLab6.collection.CollectionManager;

public class LoginCommand extends CollectionCommand {
    public LoginCommand(CollectionManager collectionManager) {
        super("login", 2, "login name password - проверить логин и пароль", collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) throws ExitCLIException {

        return null;
    }
}
