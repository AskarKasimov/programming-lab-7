package ru.askar.clientLab6.connection;

import ru.askar.clientLab6.NeedToReconnectException;

import java.io.IOException;

public interface ClientHandler {
    void start() throws IOException, NeedToReconnectException;

    void stop();

    boolean getRunning();

    void setPort(int port);

    void setHost(String host);

    void sendMessage(Object object);
}
