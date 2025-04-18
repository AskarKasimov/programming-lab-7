package ru.askar.common;

import ru.askar.common.cli.CommandResponseCode;

import java.io.Serializable;

/**
 * @param code     -1 - не писать 0 - белый 1 - зелёный 2 - жёлтый 3 - красный
 * @param response
 */
public record CommandResponse(CommandResponseCode code, String response) implements Serializable {
}
