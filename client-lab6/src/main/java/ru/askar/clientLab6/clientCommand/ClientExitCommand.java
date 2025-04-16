package ru.askar.clientLab6.clientCommand;

import ru.askar.clientLab6.connection.ClientHandler;
import ru.askar.common.CommandResponse;
import ru.askar.common.exception.ExitCLIException;

public class ClientExitCommand extends ClientCommand {
    public ClientExitCommand(ClientHandler clientHandler) {
        super("exit", 0, "exit - завершить программу", clientHandler);
    }

    @Override
    public CommandResponse execute(String[] args) throws ExitCLIException {
        clientHandler.stop();
        throw new ExitCLIException();
    }
}
