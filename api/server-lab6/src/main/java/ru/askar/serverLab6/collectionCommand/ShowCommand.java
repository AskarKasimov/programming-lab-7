package ru.askar.serverLab6.collectionCommand;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.object.Ticket;
import ru.askar.serverLab6.collection.CollectionManager;

import java.util.Arrays;

public class ShowCommand extends CollectionCommand {
    private final CollectionManager collectionManager;

    public ShowCommand(CollectionManager collectionManager) {
        super(
                "show",
                0,
                "show - вывести все элементы коллекции в строковом представлении",
                collectionManager);
        this.collectionManager = collectionManager;
    }

    @Override
    public CommandResponse execute(String[] args) {
        return new CommandResponse(
                CommandResponseCode.INFO,
                AsciiTable.getTable(
                        collectionManager.getCollection().values(),
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
