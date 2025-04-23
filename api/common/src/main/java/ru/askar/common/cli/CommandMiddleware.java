package ru.askar.common.cli;

import ru.askar.common.CommandResponse;
import ru.askar.common.exception.ExitCLIException;

public interface CommandMiddleware<T extends Command> {
    CommandResponse handle(T command, String[] args, MiddlewareChain<T> chain) throws ExitCLIException;
}