package ru.askar.common;

import java.io.Serializable;
import ru.askar.common.object.Ticket;

public record CommandToExecute(String name, String[] args, Ticket object) implements Serializable {}
