package ru.askar.serverLab6.collection;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;
import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.common.object.*;

import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 * Manager для коллекции билетов.
 */
public class CollectionManager {
    private final LocalDateTime dateOfInitialization;
    private final TreeMap<Long, Ticket> collection = new TreeMap<>();
    private final Mono<? extends Connection> connectionMono;

    public CollectionManager(ConnectionFactory connectionFactory) {
        this.dateOfInitialization = LocalDateTime.now();
        this.connectionMono = Mono.from(connectionFactory.create()).cache();
        System.out.println("Загрузка билетов из базы данных...");
        loadTicketsFromDatabase().block();
    }

    private Mono<Void> loadTicketsFromDatabase() {
        return connectionMono.flatMapMany(connection ->
                        connection.createStatement("SELECT "
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
                                        + "JOIN users u ON u.id = t.creator_id")
                                .execute()
                )
                .flatMap(result -> result.map((row, meta) -> {
                    Coordinates coordinates = new Coordinates(
                            row.get("x", Float.class),
                            row.get("y", Float.class));

                    Event event = null;
                    Integer eventId = row.get("event_id", Integer.class);
                    if (eventId != null) {
                        event = new Event(eventId,
                                row.get("event_name", String.class),
                                row.get("description", String.class),
                                EventType.valueOf(row.get("event_type", String.class)));
                    }

                    return new Ticket(
                            row.get("creation_date", LocalDateTime.class),
                            row.get("ticket_id", Long.class),
                            row.get("ticket_name", String.class),
                            coordinates,
                            row.get("price", Long.class),
                            TicketType.valueOf(row.get("ticket_type", String.class)),
                            event,
                            row.get("user_id", Integer.class));
                }))
                .doOnNext(ticket -> {
                    try {
                        validateTicket(ticket);
                    } catch (InvalidInputFieldException e) {
                        throw new RuntimeException(e);
                    }
                })
                .onErrorResume(e -> {
                    if (e.getCause() instanceof InvalidInputFieldException) {
                        System.err.println("Некорректные данные в билете (не загружаю его): " + e.getCause().getMessage());
                        return Mono.empty();
                    }
                    return Mono.error(e);
                })
                .then()
                .doOnSuccess(__ -> {
                    System.out.println("Загрузка билетов из базы данных завершена");
                });
    }

    public LocalDateTime getDateOfCreation() {
        return dateOfInitialization;
    }

    public TreeMap<Long, Ticket> getCollection() {
        // безопасно возвращаем копию коллекции
        return new TreeMap<>(collection);
    }

    public void clear() {
        collection.clear();
    }

    public void remove(Long id) {
        collection.remove(id);
    }

    public Mono<Void> putWithValidation(Ticket ticket) {
        Callable<Ticket> validationCallable = () -> {
            validateTicket(ticket);
            return ticket;
        };
        return Mono.fromCallable(validationCallable)
                .flatMap(validatedTicket -> connectionMono.flatMap(conn ->
                        Mono.from(conn.createStatement(
                                                "INSERT INTO ticket (id, creator_id, name, x, y, creation_date, price, ticket_type, event_id) " +
                                                        "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9) " +
                                                        "RETURNING id")
                                        .bind(0, validatedTicket.getId())
                                        .bind(1, validatedTicket.getCreatorId())
                                        .bind(2, validatedTicket.getName())
                                        .bind(3, validatedTicket.getCoordinates().getX())
                                        .bind(4, validatedTicket.getCoordinates().getY())
                                        .bind(5, validatedTicket.getCreationDate())
                                        .bind(6, validatedTicket.getPrice())
                                        .bind(7, validatedTicket.getType().toString())
                                        .bind(8, validatedTicket.getEvent() != null ?
                                                validatedTicket.getEvent().getId() : null)
                                        .execute())
                                .flatMap(result ->
                                        Mono.from(result.map((row, meta) -> row.get("id", Long.class)))
                                )
                                .doOnNext(generatedId -> {
                                    validatedTicket.setId(generatedId);
                                    collection.put(generatedId, validatedTicket);
                                })
                                .then()
                ))
                .doOnSuccess(__ ->
                        System.out.println("Билет сохранён. ID: " + ticket.getId())
                )
                .onErrorResume(e -> {
                    System.err.println("Ошибка: " + e.getMessage());
                    return Mono.error(e);
                });
    }

    public static void validateTicket(Ticket object) throws InvalidInputFieldException {
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
