package ru.askar.serverLab7.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab7.collection.CollectionManager;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class RemoveGreaterKeyCommand extends CollectionCommand {
    public RemoveGreaterKeyCommand(CollectionManager collectionManager) {
        super(
                "remove_greater_key",
                1,
                "remove_greater_key key - удалить элементы, ключ которых превышает заданный",
                collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        Long key;
        try {
            key = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return new CommandResponse(CommandResponseCode.ERROR, "В поле key требуется число");
        }
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        collectionManager.getCollectionValuesStream()
                .filter(t -> t.getId() > key)
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
