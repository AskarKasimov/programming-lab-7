package ru.askar.serverLab6.collectionCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.collection.CollectionManager;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class ClearCommand extends CollectionCommand {
    public ClearCommand(CollectionManager collectionManager) {
        super("clear", 0, "clear - очистить коллекцию", collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        collectionManager.getCollectionValuesStream().forEach(t -> {
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
