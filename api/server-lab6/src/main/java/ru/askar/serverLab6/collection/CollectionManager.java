package ru.askar.serverLab6.collection;

import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.common.object.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.TreeMap;

/**
 * Manager для коллекции билетов.
 */
public class CollectionManager {
    private final LocalDateTime dateOfInitialization;
    private final TreeMap<Long, Ticket> collection = new TreeMap<>();
    private final Connection connection;

    public CollectionManager(Connection connection) throws InvalidInputFieldException, IOException {
        this.dateOfInitialization = LocalDateTime.now();
        this.connection = connection;
        loadTicketsFromDatabase();
    }

    private void loadTicketsFromDatabase() {
        String sql = "SELECT "
                + "t.id AS ticket_id, "
                + "t.name AS ticket_name, "
                + "t.x, "
                + "t.y, "
                + "t.creation_date, "
                + "t.price, "
                + "t.ticket_type, "
                + "t.event_id, "
                + "e.id AS event_id, "
                + "e.name AS event_name, "
                + "e.description, "
                + "e.event_type, "
                + "u.id AS user_id "
                + "FROM ticket t "
                + "LEFT JOIN event e ON e.id = t.event_id "
                + "JOIN users u ON u.id = t.creator_id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ticket ticket = mapRowToTicket(rs);
                collection.put(ticket.getId(), ticket);
            }
            System.out.println("Коллекция успешно загружена из БД");

        } catch (SQLException e) {
            System.err.println("Ошибка загрузки билетов: " + e.getMessage());
        }
    }

    private Ticket mapRowToTicket(ResultSet rs) throws SQLException {
        Coordinates coordinates = new Coordinates(
                rs.getFloat("x"),
                rs.getFloat("y")
        );

        Event event = new Event(
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("description"),
                EventType.valueOf(rs.getString("event_type"))
        );

        return new Ticket(
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getLong("ticket_id"),
                rs.getString("ticket_name"),
                coordinates,
                rs.getLong("price"),
                TicketType.valueOf(rs.getString("ticket_type")),
                event
        );
    }

    public void putWithValidation(Ticket ticket) throws InvalidInputFieldException, SQLException {

        validateTicket(ticket);

        String sql = "INSERT INTO ticket (id, creator_id, name, x, y, creation_date, price, ticket_type, event_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, ticket.getId());
            stmt.setInt(2, ticket.getCreatorId());
            stmt.setString(3, ticket.getName());
            stmt.setFloat(4, ticket.getCoordinates().getX());
            stmt.setFloat(5, ticket.getCoordinates().getY());
            stmt.setTimestamp(6, Timestamp.valueOf(ticket.getCreationDate()));
            stmt.setLong(7, ticket.getPrice());
            stmt.setString(8, ticket.getType().toString());
            stmt.setObject(9, ticket.getEvent() != null ? ticket.getEvent().getId() : null);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    synchronized (collection) {
                        if (generatedKeys.next()) {
                            ticket.setId(generatedKeys.getLong(1));
                            collection.put(ticket.getId(), ticket);
                        }
                    }
                }
            }
        }
    }

    public LocalDateTime getDateOfCreation() {
        return dateOfInitialization;
    }

    public TreeMap<Long, Ticket> getCollection() {
        // безопасно возвращаем копию коллекции
        synchronized (collection) {
            return new TreeMap<>(collection);
        }
    }

    public void clear() {
        synchronized (collection) {
            collection.clear();
        }
    }

    public void remove(Long id) {
        synchronized (collection) {
            collection.remove(id);
        }
    }

    public static void validateTicket(Ticket object) throws InvalidInputFieldException {
        // ticket id
        if (object.getId() < 1) {
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
}
