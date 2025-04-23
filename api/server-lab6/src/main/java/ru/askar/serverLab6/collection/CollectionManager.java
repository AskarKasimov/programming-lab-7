package ru.askar.serverLab6.collection;

import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.common.object.*;

import java.io.IOException;
import java.sql.*;
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
    private final Connection connection;

    public CollectionManager(Connection connection) throws InvalidInputFieldException, IOException {
        this.dateOfInitialization = LocalDateTime.now();
        this.connection = connection;
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

        boolean idIsNull = (ticket.getId() == null);
        String sql;
        if (idIsNull) {
            sql = "INSERT INTO ticket (creator_id, name, x, y, creation_date, price, ticket_type, event_id) VALUES (?, ?, ?, ?, ?, ?, ?::TicketType, ?)";
        } else {
            sql = "INSERT INTO ticket (id, creator_id, name, x, y, creation_date, price, ticket_type, event_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?::TicketType, ?)";
        }


        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int paramIndex = 1;

            if (!idIsNull) {
                stmt.setLong(paramIndex++, ticket.getId());
            }
            if (ticket.getCreatorId() == null) stmt.setNull(paramIndex++, Types.INTEGER);
            else
                stmt.setInt(paramIndex++, ticket.getCreatorId());
            stmt.setString(paramIndex++, ticket.getName());
            stmt.setFloat(paramIndex++, ticket.getCoordinates().getX());
            stmt.setFloat(paramIndex++, ticket.getCoordinates().getY());
            stmt.setTimestamp(paramIndex++, Timestamp.valueOf(ticket.getCreationDate()));
            stmt.setLong(paramIndex++, ticket.getPrice());
            stmt.setString(paramIndex++, ticket.getType().toString());
            stmt.setObject(paramIndex, ticket.getEvent() != null ? ticket.getEvent().getId() : null);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    synchronized (collection) {
                        if (generatedKeys.next()) {
                            long newId = generatedKeys.getLong(1);
                            ticket.setId(newId);
                            collection.put(newId, ticket);
                        }
                    }
                }
            }
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

    public Ticket get(Long id) {
        synchronized (collection) {
            return collection.get(id);
        }
    }
}
