package ru.askar.common;

import ru.askar.common.object.Ticket;

import java.io.Serializable;

public record CommandToExecute(String name, String[] args, Ticket object,
                               Credentials credentials) implements Serializable {
}
