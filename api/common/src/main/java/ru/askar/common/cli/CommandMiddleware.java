package ru.askar.common.cli;

import ru.askar.common.CommandResponse;

public interface CommandMiddleware<T extends Command> {
    CommandResponse handle(T command, String[] args, MiddlewareChain<T> chain);
}