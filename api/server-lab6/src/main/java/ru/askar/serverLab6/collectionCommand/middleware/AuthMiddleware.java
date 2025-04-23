package ru.askar.serverLab6.collectionCommand.middleware;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandMiddleware;
import ru.askar.common.cli.MiddlewareChain;
import ru.askar.serverLab6.collectionCommand.CollectionCommand;

public class AuthMiddleware implements CommandMiddleware<CollectionCommand> {
    @Override
    public CommandResponse handle(CollectionCommand command, String[] args, MiddlewareChain<CollectionCommand> chain) {
        return null;
    }
}
