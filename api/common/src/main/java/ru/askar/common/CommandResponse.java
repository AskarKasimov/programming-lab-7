package ru.askar.common;

import ru.askar.common.cli.CommandResponseCode;

import java.io.Serializable;

public record CommandResponse(CommandResponseCode code, String response) implements Serializable {
}
