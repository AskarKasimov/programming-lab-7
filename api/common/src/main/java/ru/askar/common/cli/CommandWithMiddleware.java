package ru.askar.common.cli;

import ru.askar.common.CommandResponse;
import ru.askar.common.exception.ExitCLIException;

import java.util.ArrayList;
import java.util.List;

public class CommandWithMiddleware<T extends Command> extends Command {
    private final T original;
    private final List<CommandMiddleware<T>> middlewares;
    private int currentMiddleware = 0;

    public CommandWithMiddleware(T original, List<CommandMiddleware<T>> middlewares) {
        super(original.getName(), original.getArgsCount(), original.getInfo());
        this.original = original;
        this.middlewares = new ArrayList<>(middlewares);
    }

    @Override
    public CommandResponse execute(String[] args) throws ExitCLIException {
        if (currentMiddleware < middlewares.size()) {
            CommandMiddleware<T> middleware = middlewares.get(currentMiddleware);
            currentMiddleware++;
            return middleware.handle(
                    original,
                    args,
                    (cmd, a) -> execute(a)
            );
        }
        currentMiddleware = 0; // Сброс цепочки
        return original.execute(args);
    }

    public T getOriginalCommand() {
        return original;
    }
}
