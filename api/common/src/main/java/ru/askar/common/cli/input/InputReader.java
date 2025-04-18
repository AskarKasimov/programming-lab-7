package ru.askar.common.cli.input;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandExecutor;
import ru.askar.common.cli.CommandParser;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.cli.ParsedCommand;
import ru.askar.common.exception.ExitCLIException;
import ru.askar.common.exception.InvalidCommandException;
import ru.askar.common.exception.NoSuchCommandException;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;

public class InputReader {
    /**
     * Класс, ответственный за чтение ввода от пользователя и исполнение команд.
     */
    private final CommandExecutor commandExecutor;

    private final CommandParser commandParser;
    private boolean scriptMode = false;
    private BufferedReader bufferedReader;

    /**
     * @param commandExecutor - класс для выполнения команд.
     * @param commandParser   - класс для парсинга команд.
     * @param bufferedReader  - класс для чтения ввода.
     */
    public InputReader(
            CommandExecutor commandExecutor,
            CommandParser commandParser,
            BufferedReader bufferedReader) {
        this.commandExecutor = commandExecutor;
        this.commandParser = commandParser;
        this.bufferedReader = bufferedReader;
    }

    public boolean isScriptMode() {
        return scriptMode;
    }

    public void setScriptMode(boolean scriptMode) {
        this.scriptMode = scriptMode;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public void setBufferedReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    /**
     * Считыватель ввода строки.
     *
     * @return - строка, введенная пользователем.
     * @throws IllegalArgumentException - если произошла ошибка ввода.
     * @see BufferedReader
     */
    public String getInputString() {
        try {
            String line = bufferedReader.readLine();
            if (line != null && line.isEmpty()) {
                return null;
            }
            return line;
        } catch (IOException e) {
            throw new IllegalArgumentException("Ошибка ввода");
        }
    }

    public BigDecimal getInputBigDecimal() {
        // Сделано по большей части для Y и ограничения на 654.00000000000001 и прочую дичь
        try {
            BigDecimal input = new BigDecimal(bufferedReader.readLine());
            float floatValue = input.floatValue();
            // обратное преобразование сохраняет значение
            BigDecimal restored = new BigDecimal(floatValue);
            if (input.compareTo(restored) != 0) {
                throw new IllegalArgumentException(
                        "Число " + input + " нельзя адекватно запихнуть в Float");
            }
            return input;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Требуется число с точкой");
        } catch (IOException e) {
            throw new IllegalArgumentException("Ошибка ввода");
        }
    }

    /**
     * Выполнение поступающих команд.
     */
    public void process() throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            try {
                ParsedCommand parsedCommand;
                try {
                    parsedCommand = commandParser.parse(line.split(" "));
                } catch (InvalidCommandException e) {
                    commandExecutor
                            .getOutputWriter()
                            .write(CommandResponseCode.ERROR.getColoredMessage(e.getMessage()));
                    continue;
                }
                if (parsedCommand.args().length
                        != commandExecutor.getCommand(parsedCommand.name()).getArgsCount()) {
                    commandExecutor
                            .getOutputWriter()
                            .write(
                                    CommandResponseCode.ERROR.getColoredMessage(
                                            "Неверное количество аргументов: для команды "
                                                    + parsedCommand.name()
                                                    + " требуется "
                                                    + commandExecutor
                                                    .getCommand(parsedCommand.name())
                                                    .getArgsCount()));
                    continue;
                }
                CommandResponse commandResponse =
                        commandExecutor
                                .getCommand(parsedCommand.name())
                                .execute(parsedCommand.args());
                commandExecutor
                        .getOutputWriter()
                        .write(
                                commandResponse
                                        .code()
                                        .getColoredMessage(commandResponse.response()));
            } catch (NoSuchCommandException e) {
                commandExecutor
                        .getOutputWriter()
                        .write(CommandResponseCode.ERROR.getColoredMessage(e.getMessage()));
            } catch (ExitCLIException e) {
                commandExecutor
                        .getOutputWriter()
                        .write(CommandResponseCode.WARNING.getColoredMessage(e.getMessage()));
                return;
            }
            if (scriptMode) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
