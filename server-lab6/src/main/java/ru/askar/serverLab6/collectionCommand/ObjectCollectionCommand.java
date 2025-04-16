package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.object.Ticket;
import ru.askar.serverLab6.collection.CollectionManager;

public abstract class ObjectCollectionCommand extends CollectionCommand {
    protected Ticket object;

    public ObjectCollectionCommand(
            String name, int argsCount, String info, CollectionManager collectionManager) {
        super(name, argsCount, info, collectionManager);
    }

    public void setObject(Ticket object) {
        this.object = object;
    }
}
