package ru.askar.common.object;

import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.cli.input.InputReader;
import ru.askar.common.cli.output.OutputWriter;
import ru.askar.common.exception.UserRejectedToFillFieldsException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Ticket implements Comparable<Ticket>, Serializable {
    private final LocalDateTime creationDate;
    private Long id;
    private String name;
    private Coordinates coordinates;
    private long price;
    private TicketType type;
    private Event event;
    private Integer creatorId;

    public Ticket(LocalDateTime creationDate, Long id, String name, Coordinates coordinates, long price, TicketType type, Event event) {
        this.creationDate = creationDate;
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.price = price;
        this.type = type;
        this.event = event;
    }

    private Ticket(Long ticketId, String name, long price) {
        setId(ticketId);
        setName(name);
        setPrice(price);
        this.creationDate = LocalDateTime.now();
    }

    /**
     * Создание экземпляра с пользовательским вводом. Параметры помимо <code>name</code> и <code>
     * price</code> будут считываться из заданного метода
     *
     * @param outputWriter - способ печати ответа
     * @param inputReader  - способ считывания входных данных
     * @param ticketId     - id билета
     * @param name         - название
     * @param price        - цена
     * @return - созданный Ticket
     */
    public static Ticket createTicket(
            OutputWriter outputWriter,
            InputReader inputReader,
            Long ticketId,
            String name,
            long price,
            Integer eventId,
            boolean scriptMode)
            throws UserRejectedToFillFieldsException {
        Ticket ticket = new Ticket(ticketId, name, price);
        ticket.setCoordinates(Coordinates.createCoordinates(outputWriter, inputReader, scriptMode));
        ticket.setType(TicketType.createTicketType(outputWriter, inputReader, scriptMode));
        ticket.requestEvent(outputWriter, inputReader, eventId, scriptMode);
        return ticket;
    }

    private void requestEvent(
            OutputWriter outputWriter, InputReader inputReader, Integer eventId, boolean scriptMode)
            throws UserRejectedToFillFieldsException {
        outputWriter.write(
                CommandResponseCode.WARNING.getColoredMessage("Хотите ввести событие? (y/n): "));
        String answer = inputReader.getInputString();
        if (answer != null && answer.equalsIgnoreCase("y")) {
            this.setEvent(Event.createEvent(outputWriter, inputReader, eventId, scriptMode));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return price == ticket.price
                && Objects.equals(id, ticket.id)
                && Objects.equals(name, ticket.name)
                && Objects.equals(coordinates, ticket.coordinates)
                && Objects.equals(creationDate, ticket.creationDate)
                && type == ticket.type
                && Objects.equals(event, ticket.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, price, type, event);
    }

    /**
     * Сравнение, реализованное через разницу id'шников
     */
    @Override
    public int compareTo(Ticket other) {
        // Сначала сравниваем по типу билета
        int typeComparison = this.type.compareTo(other.type);
        if (typeComparison != 0) {
            return typeComparison;
        }

        // Если типы равны, сравниваем по цене
        return Long.compare(this.price, other.price);
    }

    @Override
    public String toString() {
        return "Билет"
                + ": id="
                + id
                + ", название='"
                + name
                + "'"
                + ", координаты="
                + coordinates
                + ", дата создания="
                + creationDate
                + ", цена="
                + price
                + ", тип="
                + type
                + ", событие="
                + event
                + ";";
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public TicketType getType() {
        return type;
    }

    public void setType(TicketType type) {
        this.type = type;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }
}
