package ru.askar.serverLab7.collectionCommand;

import ru.askar.common.Credentials;
import ru.askar.common.cli.Command;
import ru.askar.serverLab7.collection.CollectionManager;

public abstract class CollectionCommand extends Command {
    protected final CollectionManager collectionManager;
    protected Credentials credentials;

    /**
     * Заполнение имени и количества требуемых аргументов
     *
     * @param name
     * @param argsCount
     */
    public CollectionCommand(
            String name, int argsCount, String info, CollectionManager collectionManager) {
        super(name, argsCount, info);
        this.collectionManager = collectionManager;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}
