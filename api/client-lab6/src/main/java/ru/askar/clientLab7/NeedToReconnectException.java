package ru.askar.clientLab7;

public class NeedToReconnectException extends Exception {
    public NeedToReconnectException(int depth) {
        super("Need to reconnect to server, depth: " + depth);
    }
}
