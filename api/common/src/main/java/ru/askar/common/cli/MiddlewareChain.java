package ru.askar.common.cli;

import ru.askar.common.CommandResponse;
import ru.askar.common.exception.ExitCLIException;

public interface MiddlewareChain<T extends Command> {
    CommandResponse proceed(T command, String[] args) throws ExitCLIException;
}