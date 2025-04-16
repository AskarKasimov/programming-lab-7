package ru.askar.common;

import java.io.Serializable;
import ru.askar.common.cli.CommandResponseCode;

/**
 * @param code -1 - не писать 0 - белый 1 - зелёный 2 - жёлтый 3 - красный
 * @param response
 */
public record CommandResponse(CommandResponseCode code, String response) implements Serializable {}
