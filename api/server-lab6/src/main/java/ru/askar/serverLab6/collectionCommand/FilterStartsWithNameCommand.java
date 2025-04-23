package ru.askar.serverLab6.collectionCommand;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.object.Ticket;
import ru.askar.serverLab6.collection.CollectionManager;

import java.util.Arrays;
import java.util.List;

public class FilterStartsWithNameCommand extends CollectionCommand {
    public FilterStartsWithNameCommand(CollectionManager collectionManager) {
        super(
                "filter_starts_with_name",
                1,
                "filter_starts_with_name name - вывести элементы, значение поля name которых начинается с заданной подстроки",
                collectionManager);
    }

    @Override
    public CommandResponse execute(String[] args) {
        List<Ticket> ticketList =
                collectionManager.getCollectionValuesStream()
                        .filter(t -> t.getName().startsWith(args[0]))
                        .toList();
        return new CommandResponse(
                CommandResponseCode.INFO,
                AsciiTable.getTable(
                        ticketList,
                        Arrays.asList(
                                new Column()
                                        .header("ID")
                                        .maxWidth(10)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(ticket -> String.valueOf(ticket.getId())),
                                new Column()
                                        .header("Название")
                                        .maxWidth(10)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(Ticket::getName),
                                new Column()
                                        .header("Координаты")
                                        .maxWidth(31)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(
                                                ticket ->
                                                        "("
                                                                + ticket.getCoordinates().getX()
                                                                + ", "
                                                                + ticket.getCoordinates().getY()
                                                                + ")"),
                                new Column()
                                        .header("Дата создания")
                                        .maxWidth(31)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(ticket -> ticket.getCreationDate().toString()),
                                new Column()
                                        .header("Цена")
                                        .maxWidth(10)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(ticket -> String.valueOf(ticket.getPrice())),
                                new Column()
                                        .header("Тип")
                                        .maxWidth(10)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(ticket -> ticket.getType().name()),
                                new Column()
                                        .header("ID события")
                                        .maxWidth(10)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(
                                                ticket ->
                                                        ticket.getEvent() != null
                                                                ? String.valueOf(
                                                                ticket.getEvent().getId())
                                                                : "-"),
                                new Column()
                                        .header("Название события")
                                        .maxWidth(10)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(
                                                ticket ->
                                                        ticket.getEvent() != null
                                                                ? ticket.getEvent().getName()
                                                                : "-"),
                                new Column()
                                        .header("Описание события")
                                        .maxWidth(20)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(
                                                ticket ->
                                                        ticket.getEvent() != null
                                                                ? ticket.getEvent().getDescription()
                                                                : "-"),
                                new Column()
                                        .header("Тип события")
                                        .maxWidth(10)
                                        .headerAlign(HorizontalAlign.CENTER)
                                        .with(
                                                ticket ->
                                                        ticket.getEvent() != null
                                                                && ticket.getEvent()
                                                                .getEventType()
                                                                != null
                                                                ? ticket.getEvent()
                                                                .getEventType()
                                                                .name()
                                                                : "-"))));
    }
}
