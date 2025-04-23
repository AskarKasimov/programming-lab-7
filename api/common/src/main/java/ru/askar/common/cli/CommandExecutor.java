package ru.askar.common.cli;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.output.OutputWriter;
import ru.askar.common.exception.ExitCLIException;
import ru.askar.common.exception.NoSuchCommandException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для аккумулирования команд и предоставления к ним доступа.
 */
public class CommandExecutor<T extends Command> {
    protected final Map<String, CommandWithMiddleware<T>> commands = new LinkedHashMap<>();
    protected OutputWriter outputWriter;

    public CommandExecutor() {
    }

    public OutputWriter getOutputWriter() {
        return outputWriter;
    }

    public void setOutputWriter(OutputWriter outputWriter) {
        this.outputWriter = outputWriter;
    }

    /**
     * Добавление команды.
     *
     * @param command - команда
     */
    @SafeVarargs
    public final void register(T command, CommandMiddleware<T>... middlewares) {
        List<CommandMiddleware<T>> middlewareList = Arrays.asList(middlewares);
        commands.put(command.getName(), new CommandWithMiddleware<>(command, middlewareList));
    }

    public void clearCommands() {
        commands.clear();
    }

    public CommandResponse execute(String commandName, String[] args) throws ExitCLIException, NoSuchCommandException {
        CommandWithMiddleware<T> command = commands.get(commandName);
        if (command == null) {
            throw new NoSuchCommandException("Неизвестная команда: " + commandName);
        }
        return command.execute(args);
    }

    public void validateCommand(String commandName, int argsCount) throws NoSuchCommandException {
        CommandWithMiddleware<T> command;
        try {
            command = commands.get(commandName);
        } catch (NullPointerException e) {
            throw new NoSuchCommandException("Неизвестная команда: " + commandName);
        }
        if (argsCount != command.getArgsCount()) {
            throw new IllegalArgumentException(
                    "Неверное количество аргументов для команды " + commandName + ". Ожидалось: " + command.getArgsCount());
        }
    }

    /**
     * Копия экземпляра мапы со всеми доступными командами
     */
    public Map<String, CommandWithMiddleware<T>> getAllCommands() {
        return new LinkedHashMap<>(commands);
    }
}
