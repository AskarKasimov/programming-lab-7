package ru.askar.clientLab7.clientCommand;

import ru.askar.clientLab7.connection.ClientHandler;
import ru.askar.common.CommandResponse;
import ru.askar.common.cli.Command;
import ru.askar.common.cli.CommandExecutor;
import ru.askar.common.cli.CommandResponseCode;

import java.util.ArrayList;
import java.util.List;

public class ClientHelpCommand extends ClientCommand {
    private final CommandExecutor<ClientCommand> executor;

    /**
     * Заполнение имени и количества требуемых аргументов
     */
    public ClientHelpCommand(ClientHandler serverHandler, CommandExecutor<ClientCommand> executor) {
        super("help", 0, "help - вывести справку по доступным серверным командам", serverHandler);
        this.executor = executor;
    }

    @Override
    public CommandResponse execute(String[] args) {
        StringBuilder builder = new StringBuilder();
        List<Command> commands = new ArrayList<>(executor.getAllCommands().values());
        commands.forEach(command -> builder.append(command.getInfo()).append("\n"));
        return new CommandResponse(
                CommandResponseCode.INFO, builder.substring(0, builder.length() - 1));
    }
}
