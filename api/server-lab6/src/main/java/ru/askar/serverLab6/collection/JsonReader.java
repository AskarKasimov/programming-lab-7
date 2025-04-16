package ru.askar.serverLab6.collection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import ru.askar.common.exception.InvalidCollectionFileException;
import ru.askar.common.object.Ticket;

public class JsonReader implements DataReader {
    private final TreeMap<Long, Ticket> collection = new TreeMap<>();
    private final String source;
    private final BufferedInputStream inputStream;

    public JsonReader(String source, BufferedInputStream inputStream) {
        this.source = source;
        this.inputStream = inputStream;
    }

    @Override
    public void readData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ArrayList<Ticket> tempMap = objectMapper.readValue(inputStream, new TypeReference<>() {});
        TreeMap<Long, Ticket> tickets = new TreeMap<>();
        tempMap.forEach(
                ticket -> {
                    if (tickets.containsKey(ticket.getId())) {
                        throw new InvalidCollectionFileException(
                                "были обнаружены билеты (Ticket) с одинаковыми id");
                    }
                    tickets.put(ticket.getId(), ticket);
                });
        this.collection.clear();
        this.collection.putAll(tickets);
        try {
            validateData();
        } catch (InvalidCollectionFileException e) {
            this.collection.clear();
            throw e;
        }
    }

    @Override
    public TreeMap<Long, Ticket> getData() {
        return collection;
    }

    @Override
    public String getSource() {
        return source;
    }

    private void validateData() throws InvalidCollectionFileException {
        ArrayList<Integer> eventsIds =
                new ArrayList<>(
                        collection.values().stream()
                                .map(
                                        (ticket ->
                                                ticket.getEvent() != null
                                                        ? ticket.getEvent().getId()
                                                        : 0))
                                .toList());
        collection.forEach(
                (id, ticket) -> {
                    if (ticket.getEvent() != null
                            && Collections.frequency(eventsIds, ticket.getEvent().getId()) > 1) {
                        throw new InvalidCollectionFileException(
                                "были обнаружены события (Event) с одинаковыми id");
                    }
                });
    }
}
