package ru.askar.serverLab6.database;

import org.apache.commons.codec.digest.DigestUtils;
import ru.askar.common.Credentials;
import ru.askar.common.object.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLConnection {
    private final Connection connection;

    public SQLConnection(Connection connection) {
        this.connection = connection;
    }

    public int removeTicket(Long id, Credentials credentials) throws SQLException {
        String sql = "DELETE FROM ticket WHERE ticket.id = ? AND ticket.creator_id IN (SELECT id FROM users WHERE users.name=? AND users.password_hash=?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, credentials.username());
            stmt.setString(3, DigestUtils.sha384Hex(credentials.password()));
            return stmt.executeUpdate();
        }
    }


    public Integer getUserId(Credentials credentials) throws SQLException {
        String sql = "SELECT id FROM users WHERE name = ? AND password_hash = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, credentials.username());
            stmt.setString(2, DigestUtils.sha384Hex(credentials.password()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return null;
                }
            }
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

    public List<Ticket> getAllTickets() throws SQLException {
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
            List<Ticket> tickets = new ArrayList<>();
            while (rs.next()) {
                Ticket ticket = mapRowToTicket(rs);
                tickets.add(ticket);
            }
            return tickets;
        }
    }

    public Long putTicket(Ticket ticket) throws SQLException {
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
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
            return null;
        }
    }
}

