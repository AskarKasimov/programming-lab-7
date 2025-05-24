package ru.askar.clientLab7.clientCommand;

import ru.askar.clientLab7.connection.ClientHandler;
import ru.askar.common.CommandAsList;
import ru.askar.common.CommandResponse;
import ru.askar.common.CommandToExecute;
import ru.askar.common.Credentials;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.cli.input.InputReader;
import ru.askar.common.cli.output.OutputWriter;
import ru.askar.common.exception.UserRejectedToFillFieldsException;
import ru.askar.common.object.Ticket;

public class ClientGenericCommand extends ClientCommand {
    private final InputReader<ClientCommand> inputReader;
    private final OutputWriter outputWriter;
    private final ClientHandler clientHandler;
    private final boolean needObject;
    private final Credentials credentials;

    /**
     * Заполнение имени и количества требуемых аргументов
     *
     * @param inputReader
     */
    public ClientGenericCommand(
            InputReader<ClientCommand> inputReader,
            CommandAsList rawCommand,
            ClientHandler clientHandler,
            OutputWriter outputWriter, Credentials credentials) {
        super(rawCommand.name(), rawCommand.args(), null, clientHandler);
        this.clientHandler = clientHandler;
        this.needObject = rawCommand.needObject();
        this.outputWriter = outputWriter;
        this.inputReader = inputReader;
        this.credentials = credentials;
    }

    @Override
    public CommandResponse execute(String[] args) {
        if (needObject) {
            String ticketName = args[args.length - 2];
            long price;
            try {
                price = Long.parseLong(args[args.length - 1]);
            } catch (NumberFormatException e) {
                return new CommandResponse(
                        CommandResponseCode.ERROR, "В поле price требуется число (Long)");
            }

            Long id;
            if (args.length == 2) {
                id = null;
            } else if (args[args.length - 3].equalsIgnoreCase("null")) {
                id = null;
            } else {
                try {
                    id = Long.parseLong(args[0]);
                } catch (NumberFormatException e) {
                    return new CommandResponse(
                            CommandResponseCode.ERROR, "В поле id требуется число");
                }
            }
            try {
                Ticket ticket;
                ticket =
                        Ticket.createTicket(
                                outputWriter, inputReader, id, ticketName, price, null, scriptMode);
                clientHandler.sendMessage(new CommandToExecute(this.name, args, ticket, credentials));
            } catch (UserRejectedToFillFieldsException e) {
                return new CommandResponse(CommandResponseCode.ERROR, e.getMessage());
            }
        } else {
            clientHandler.sendMessage(new CommandToExecute(this.name, args, null, credentials));
        }
        return new CommandResponse(CommandResponseCode.HIDDEN, "Команда отправлена на сервер");
    }
}
