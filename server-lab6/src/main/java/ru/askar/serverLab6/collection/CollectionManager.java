package ru.askar.serverLab6.collection;

import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import ru.askar.common.exception.InvalidCollectionFileException;
import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.common.object.Event;
import ru.askar.common.object.Ticket;

/** Manager для коллекции билетов. */
public class CollectionManager {
    private final LocalDateTime dateOfInitialization;
    private final TreeMap<Long, Ticket> collection = new TreeMap<>();
    private final DataReader starterDataReader;

    public CollectionManager(DataReader dataReader) throws InvalidInputFieldException, IOException {
        this.dateOfInitialization = LocalDateTime.now();
        if (dataReader == null) {
            starterDataReader =
                    new DataReader() {
                        @Override
                        public void readData() {}

                        @Override
                        public TreeMap<Long, Ticket> getData() {
                            return new TreeMap<>();
                        }

                        @Override
                        public String getSource() {
                            return null;
                        }
                    };
        } else starterDataReader = dataReader;
        try {
            starterDataReader.readData();
        } catch (JsonMappingException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                throw new InvalidInputFieldException(
                        "Критическая ошибка поля структуры: " + cause.getMessage());
            } else {
                throw new IOException(
                        "Неизвестная ошибка считывания данных из файла: " + e.getOriginalMessage());
            }
        } catch (InvalidCollectionFileException e) {
            throw new InvalidCollectionFileException(
                    "Критическая ошибка читаемого файла: " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("Ошибка при чтении файла: " + e.getMessage());
        }
        for (Ticket ticket : starterDataReader.getData().values()) {
            putWithValidation(ticket);
        }
    }

    public String getStarterSource() {
        return starterDataReader.getSource();
    }

    public Long generateNextTicketId() {
        long min = 1;
        while (collection.containsKey(min)) {
            min++;
        }
        return min;
    }

    public Integer generateNextEventId() {
        Set<Integer> ids =
                collection.values().stream()
                        .map(Ticket::getEvent)
                        .filter(Objects::nonNull)
                        .map(Event::getId)
                        .collect(Collectors.toSet());

        int min = 1;
        while (ids.contains(min)) {
            min++;
        }
        return min;
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

    public void putWithValidation(Ticket ticket) throws InvalidInputFieldException {
        validateTicket(ticket);
        collection.put(ticket.getId(), ticket);
    }

    public void validateTicket(Ticket object) throws InvalidInputFieldException {
        // ticket id
        if (object.getId() == null) {
            throw new InvalidInputFieldException("Поле id не может быть null");
        }
        if (object.getId() < 1) {
            throw new InvalidInputFieldException("Поле id должно быть больше 0");
        }
        if (collection.containsKey(object.getId())) {
            throw new InvalidInputFieldException("Элемент с таким id уже существует");
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
            if (collection.values().stream()
                    .map(Ticket::getEvent)
                    .map(Event::getId)
                    .filter(Objects::nonNull)
                    .anyMatch(id -> id.equals(object.getEvent().getId()))) {
                throw new InvalidInputFieldException("Event с таким id уже существует");
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
