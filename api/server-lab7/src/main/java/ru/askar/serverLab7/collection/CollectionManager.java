package ru.askar.serverLab7.collection;

import ru.askar.common.Credentials;
import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.common.object.Event;
import ru.askar.common.object.Ticket;
import ru.askar.serverLab7.database.SQLConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Manager для коллекции билетов.
 */
public class CollectionManager {
    private final LocalDateTime dateOfInitialization;
    private final TreeMap<Long, Ticket> collection = new TreeMap<>();
    private final SQLConnection connection;

    public CollectionManager(Connection connection) throws InvalidInputFieldException, IOException, SQLException {
        this.dateOfInitialization = LocalDateTime.now();
        this.connection = new SQLConnection(connection);
        loadTicketsFromDatabase();
    }

    public static void validateTicket(Ticket object) throws InvalidInputFieldException {
        // ticket id
        if (object.getId() != null && object.getId() < 1) {
            throw new InvalidInputFieldException("Поле id должно быть больше 0");
        }
        // ticket name
        if (object.getName() == null) {
            throw new InvalidInputFieldException("Поле name не может быть null");
        }
        if (object.getName().isEmpty()) {
            throw new InvalidInputFieldException("Поле name не может быть пустым");
        }
        // ticket coordinates
        if (object.getCoordinates() == null) {
            throw new InvalidInputFieldException("Поле coordinates не может быть null");
        }
        if (object.getCoordinates().getX() == null) {
            throw new InvalidInputFieldException("Поле coordinates.x не может быть null");
        }
        if (object.getCoordinates().getY() == null) {
            throw new InvalidInputFieldException("Поле coordinates.y не может быть null");
        }
        if (object.getCoordinates().getY() > 654) {
            throw new InvalidInputFieldException("Поле coordinates.y не может быть больше 654");
        }
        // ticket creationDate
        if (object.getCreationDate() == null) {
            throw new InvalidInputFieldException("Поле creationDate не может быть null");
        }
        // ticket price
        if (object.getPrice() < 1) {
            throw new InvalidInputFieldException("Поле price должно быть больше 0");
        }
        // ticket type
        if (object.getType() == null) {
            throw new InvalidInputFieldException("Поле type не может быть null");
        }
        // ticket event
        if (object.getEvent() != null) {
            // event id
            if (object.getEvent().getId() == null) {
                throw new InvalidInputFieldException("Поле event.id не может быть null");
            }
            if (object.getEvent().getId() < 1) {
                throw new InvalidInputFieldException("Поле event.id должно быть больше 0");
            }
            // event name
            if (object.getEvent().getName() == null) {
                throw new InvalidInputFieldException("Поле event.name не может быть null");
            }
            if (object.getEvent().getName().isEmpty()) {
                throw new InvalidInputFieldException("Поле event.name не может быть пустым");
            }
            // event description
            if (object.getEvent().getDescription() == null) {
                throw new InvalidInputFieldException("Поле event.description не может быть null");
            }
            if (object.getEvent().getDescription().length() > 1573) {
                throw new InvalidInputFieldException(
                        "Поле event.description не может быть больше 1573");
            }
        }
    }

    private void loadTicketsFromDatabase() throws SQLException, InvalidInputFieldException {
        for (Ticket ticket : connection.getAllTickets()) {
            validateTicket(ticket);
            collection.put(ticket.getId(), ticket);
        }
    }


    public Long putWithValidation(Ticket ticket, Credentials credentials) throws InvalidInputFieldException, SQLException {
        synchronized (collection) {
            Event event = ticket.getEvent();
            if (event != null) {
                //событие с такими же данными
                Integer existingEventId = connection.findMatchingEvent(event);
                if (existingEventId != null) {
                    event.setId(existingEventId);
                } else {
                    Integer newEventId = connection.addEvent(event);
                    if (newEventId == null) {
                        throw new SQLException("Не удалось добавить событие в базу данных");
                    }
                    event.setId(newEventId);
                }
            }
            validateTicket(ticket);
            Long id = connection.putTicket(ticket, credentials);
            if (id == null) {
                throw new SQLException("Не удалось добавить билет в базу данных");
            }
            ticket.setId(id);
            collection.put(id, ticket);
            return id;
        }
    }


    public LocalDateTime getDateOfCreation() {
        return dateOfInitialization;
    }

    public Stream<Ticket> getCollectionValuesStream() {
        synchronized (collection) {
            // Создаём копию значений, чтобы стрим был независим от оригинальной коллекции
            return new ArrayList<>(collection.values()).stream();
        }
    }

    public int remove(Long id, Credentials credentials) throws SQLException {
        synchronized (collection) {
            int deleted = connection.removeTicket(id, credentials);
            if (deleted == 1) collection.remove(id);
            throw new SQLException("Не удалось удалить билет");
        }
    }

    public Ticket get(Long id) {
        synchronized (collection) {
            return collection.get(id);
        }
    }
}
