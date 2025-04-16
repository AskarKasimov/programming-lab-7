package ru.askar.clientLab6.connection;

import java.io.IOException;
import ru.askar.clientLab6.NeedToReconnectException;

public interface ClientHandler {
    void start() throws IOException, NeedToReconnectException;

    void stop();

    boolean getRunning();

    void setPort(int port);

    void setHost(String host);

    void sendMessage(Object object);
}
