package ru.askar.serverLab6.connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ServerHandler {
    void start() throws IOException;

    void stop();

    boolean getStatus();

    int getPort();

    void setPort(int port);

    void sendMessage(SocketChannel channel, Object message);
}
