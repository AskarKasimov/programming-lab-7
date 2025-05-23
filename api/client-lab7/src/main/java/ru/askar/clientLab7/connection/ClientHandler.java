package ru.askar.clientLab7.connection;

import ru.askar.clientLab7.NeedToReconnectException;
import ru.askar.common.Credentials;

import java.io.IOException;

public interface ClientHandler {
    void start() throws IOException, NeedToReconnectException;

    void stop();

    boolean getRunning();

    void setPort(int port);

    void setHost(String host);

    void sendMessage(Object object);

    Credentials getCredentials();

    void setCredentials(Credentials credentials);
}
