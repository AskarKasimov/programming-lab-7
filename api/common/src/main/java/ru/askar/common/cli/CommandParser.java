package ru.askar.common.cli;

import ru.askar.common.exception.InvalidCommandException;

import java.util.Arrays;

/**
 * Класс для разграничения имени команды и аргументов к ней
 *
 * @see ParsedCommand
 */
public class CommandParser {
    /**
     * @param args - вся строка с поступившей командой
     * @return record с разделенным именем и аргументами
     * @throws InvalidCommandException - если строка пуста
     */
    public ParsedCommand parse(String[] args) throws InvalidCommandException {
        if (args.length == 0 || args[0].isEmpty()) {
            throw new InvalidCommandException("Команда не указана");
        }
        String commandName = args[0];
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
        return new ParsedCommand(commandName, commandArgs);
    }
}
