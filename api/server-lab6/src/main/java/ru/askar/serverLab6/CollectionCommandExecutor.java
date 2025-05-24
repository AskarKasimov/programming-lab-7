package ru.askar.serverLab6;

import ru.askar.common.CommandResponse;
import ru.askar.common.Credentials;
import ru.askar.common.cli.CommandExecutor;
import ru.askar.common.exception.ExitCLIException;
import ru.askar.common.exception.NoSuchCommandException;
import ru.askar.common.object.Ticket;
import ru.askar.serverLab6.collectionCommand.CollectionCommand;
import ru.askar.serverLab6.collectionCommand.ObjectCollectionCommand;

public class CollectionCommandExecutor extends CommandExecutor<CollectionCommand> {
    public CommandResponse execute(
            String commandName,
            String[] args,
            Ticket object,
            Credentials credentials
    ) throws ExitCLIException, NoSuchCommandException {
        validateCommand(commandName, args.length);

        CollectionCommand originalCommand = commands.get(commandName);
        originalCommand.setCredentials(credentials);
        if (originalCommand instanceof ObjectCollectionCommand) {
            ((ObjectCollectionCommand) originalCommand).setObject(object);
        }
        return originalCommand.execute(args);
    }
}
