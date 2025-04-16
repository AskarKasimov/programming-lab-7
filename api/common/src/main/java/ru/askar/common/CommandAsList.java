package ru.askar.common;

import java.io.Serializable;

public record CommandAsList(String name, int args, boolean needObject) implements Serializable {}
