package ru.askar.common.cli;

import ru.askar.common.cli.output.OutputWriter;
import ru.askar.common.exception.NoSuchCommandException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Класс для аккумулирования команд и предоставления к ним доступа.
 */
public class CommandExecutor<T extends Command> {
    private final LinkedHashMap<String, T> commands = new LinkedHashMap<>();
    private final boolean scriptMode;
    private OutputWriter outputWriter;

    public CommandExecutor() {
        this.scriptMode = false;
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
    public void register(T command) {
        command.setScriptMode(this.scriptMode);
        commands.put(command.getName(), command);
    }

    public void clearCommands() {
        commands.clear();
    }

    /**
     * Получить команду по названию
     *
     * @param name - название команды
     * @return экземпляр команды
     * @throws NoSuchCommandException - если нет команды с таким названием
     */
    public T getCommand(String name) throws NoSuchCommandException {
        T command = commands.get(name);
        if (command == null) {
            throw new NoSuchCommandException(name);
        }
        return commands.get(name);
    }

    /**
     * Копия экземпляра мапы со всеми доступными командами
     */
    public Map<String, T> getAllCommands() {
        return new LinkedHashMap<>(commands);
    }
}
