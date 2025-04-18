package ru.askar.serverLab6.collection;

import ru.askar.common.object.Ticket;

import java.io.IOException;
import java.util.TreeMap;

public interface DataReader {
    void readData() throws IOException;

    TreeMap<Long, Ticket> getData();

    String getSource();
}
