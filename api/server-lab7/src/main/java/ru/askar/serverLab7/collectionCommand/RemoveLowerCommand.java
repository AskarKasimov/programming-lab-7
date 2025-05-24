package ru.askar.serverLab7.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.serverLab7.collection.CollectionManager;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.askar.serverLab7.collection.CollectionManager.validateTicket;

public class RemoveLowerCommand extends ObjectCollectionCommand {
    public RemoveLowerCommand(CollectionManager collectionManager) {
        super(
                "remove_lower",
                2,
                "remove_lower name price - удалить из коллекции все элементы, меньшие, чем заданный",
                collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        try {
            validateTicket(object);
        } catch (InvalidInputFieldException e) {
            return new CommandResponse(CommandResponseCode.ERROR, e.getMessage());
        }
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        collectionManager.getCollectionValuesStream()
                .filter(t -> t.compareTo(object) > 0)
                .forEach(t -> {
                    try {
                        int removed = collectionManager.remove(t.getId(), credentials);
                        count.addAndGet(removed);
                    } catch (SQLException e) {
                        errorCount.incrementAndGet();
                    }
                });
        if (count.get() == 0 && errorCount.get() == 0) {
            return new CommandResponse(CommandResponseCode.ERROR, "Ваших элементов не найдено");
        } else if (errorCount.get() > 0) {
            return new CommandResponse(CommandResponseCode.SUCCESS,
                    String.format("Удалено %d элементов, но возникло %d ошибок.", count.get(), errorCount.get()));
        } else {
            return new CommandResponse(CommandResponseCode.SUCCESS,
                    String.format("Удалено %d ваших элементов.", count.get()));
        }
    }
}
