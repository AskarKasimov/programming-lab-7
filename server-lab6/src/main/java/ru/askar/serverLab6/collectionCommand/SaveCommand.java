package ru.askar.serverLab6.collectionCommand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.FileOutputStream;
import java.io.IOException;
import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.collection.CollectionManager;

public class SaveCommand extends CollectionCommand {
    public SaveCommand(CollectionManager collectionManager) {
        super("save", 0, "save - сохранить коллекцию в файл", collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        if (collectionManager.getStarterSource() == null
                || collectionManager.getStarterSource().isEmpty()) {
            return new CommandResponse(
                    CommandResponseCode.ERROR,
                    "Невозможно сохранить коллекцию в файл, так как исходный файл не был указан.");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Отключаем вывод даты в виде массива
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try (FileOutputStream fileOutputStream =
                new FileOutputStream(collectionManager.getStarterSource())) {
            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(fileOutputStream, collectionManager.getCollection().values());
            return new CommandResponse(
                    CommandResponseCode.SUCCESS,
                    "JSON успешно записан в первоначальный серверный файл "
                            + collectionManager.getStarterSource());
        } catch (IOException e) {
            return new CommandResponse(
                    CommandResponseCode.ERROR,
                    "Ошибка при записи коллекции в файл " + e.getMessage());
        }
    }
}
