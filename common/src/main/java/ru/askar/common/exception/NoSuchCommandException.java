package ru.askar.common.exception;

public class NoSuchCommandException extends Exception {
    public NoSuchCommandException(String command) {
        super("Нет такой команды: " + command);
    }
}
