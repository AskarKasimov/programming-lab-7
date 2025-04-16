package ru.askar.common.cli;

import java.io.Serializable;

public enum CommandResponseCode implements Serializable {
    HIDDEN(""),
    INFO(""),
    SUCCESS("\u001B[32m"),
    WARNING("\u001B[33m"),
    ERROR("\u001B[31m");

    private final String color;
    private static final String RESET = "\u001B[0m";

    CommandResponseCode(String color) {
        this.color = color;
    }

    public String getColoredMessage(String message) {
        if (this == HIDDEN) {
            return "";
        }
        if (this == INFO) {
            return message;
        }
        return color + message + RESET;
    }
}
