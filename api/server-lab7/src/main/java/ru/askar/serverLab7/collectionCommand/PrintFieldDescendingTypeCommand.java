package ru.askar.serverLab7.collectionCommand;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.object.Ticket;
import ru.askar.common.object.TicketType;
import ru.askar.serverLab7.collection.CollectionManager;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PrintFieldDescendingTypeCommand extends CollectionCommand {
    public PrintFieldDescendingTypeCommand(CollectionManager collectionManager) {
        super(
                "print_field_descending_type",
                0,
                "print_field_descending_type - вывести значения поля type всех элементов в порядке убывания",
                collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        List<TicketType> ticketTypes =
                collectionManager.getCollectionValuesStream()
                        .map(Ticket::getType)
                        .filter(Objects::nonNull)
                        .sorted()
                        .toList();
        return new CommandResponse(
                CommandResponseCode.INFO,
                AsciiTable.getTable(
                        ticketTypes,
                        Collections.singletonList(
                                new Column()
                                        .header("Тип")
                                        .maxWidth(10)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(Enum::name))));
    }
}
