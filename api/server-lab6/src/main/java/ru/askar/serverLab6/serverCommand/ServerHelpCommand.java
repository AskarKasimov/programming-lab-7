package ru.askar.serverLab6.serverCommand;

import ru.askar.common.CommandResponse;
import ru.askar.common.cli.Command;
import ru.askar.common.cli.CommandExecutor;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.connection.ServerHandler;

import java.util.ArrayList;
import java.util.List;

public class ServerHelpCommand extends ServerCommand {
    private final CommandExecutor<ServerCommand> executor;

    /**
     * Заполнение имени и количества требуемых аргументов
     */
    public ServerHelpCommand(ServerHandler serverHandler, CommandExecutor<ServerCommand> executor) {
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
