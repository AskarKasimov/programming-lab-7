package ru.askar.serverLab6;

import ru.askar.common.CommandAsList;
import ru.askar.common.cli.CommandExecutor;
import ru.askar.common.cli.CommandParser;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.cli.input.InputReader;
import ru.askar.common.cli.output.OutputWriter;
import ru.askar.common.cli.output.Stdout;
import ru.askar.common.exception.InvalidInputFieldException;
import ru.askar.serverLab6.collection.CollectionManager;
import ru.askar.serverLab6.collectionCommand.*;
import ru.askar.serverLab6.connection.ServerHandler;
import ru.askar.serverLab6.connection.TcpServerHandler;
import ru.askar.serverLab6.serverCommand.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        String dbHost = System.getenv("DB_HOST");
        if (dbHost == null) {
            dbHost = "localhost";
        }
        String dbPort = System.getenv("DB_PORT");
        if (dbPort == null) {
            System.out.println(
                    CommandResponseCode.ERROR.getColoredMessage("Не задан порт БД!"));
            return;
        }
        int dbPortInteger;
        try {
            dbPortInteger = Integer.parseInt(dbPort);
        } catch (NumberFormatException e) {
            System.out.println(
                    CommandResponseCode.ERROR.getColoredMessage("Порт БД должен быть числом!"));
            return;
        }
        String dbUser = System.getenv("DB_USER");
        if (dbUser == null) {
            System.out.println(
                    CommandResponseCode.ERROR.getColoredMessage("Не задан пользователь БД!"));
            return;
        }
        String dbName = System.getenv("DB_NAME");
        if (dbName == null) {
            System.out.println(
                    CommandResponseCode.ERROR.getColoredMessage("Не задана БД!"));
            return;
        }
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null) {
            System.out.println(
                    CommandResponseCode.ERROR.getColoredMessage("Не задан пароль БД!"));
            return;
        }
        CollectionManager collectionManager;
        try {
            collectionManager = new CollectionManager(DriverManager.getConnection("jdbc:postgresql://"
                            + dbHost
                            + ":"
                            + dbPort
                            + "/"
                            + dbName,
                    dbUser,
                    dbPassword));
        } catch (InvalidInputFieldException | SQLException | IOException e) {
            System.out.println(
                    CommandResponseCode.ERROR.getColoredMessage("Ошибка инициализации коллекции: " + e.getMessage()));
            return;
        }
        if (collectionManager.getCollection().isEmpty()) {
            System.out.println(CommandResponseCode.WARNING.getColoredMessage("Коллекция пуста"));
        }
        ArrayList<CommandAsList> commandList = new ArrayList<>();
        CommandExecutor<CollectionCommand> collectionCommandExecutor = new CommandExecutor<>();

        ServerHandler serverHandler = new TcpServerHandler(collectionCommandExecutor, commandList);
        collectionCommandExecutor.register(new HelpCommand(collectionCommandExecutor));
        collectionCommandExecutor.register(new InfoCommand(collectionManager));
        collectionCommandExecutor.register(new ShowCommand(collectionManager));
        collectionCommandExecutor.register(new InsertCommand(collectionManager));
        collectionCommandExecutor.register(new UpdateCommand(collectionManager));
        collectionCommandExecutor.register(new RemoveByKeyCommand(collectionManager));
        collectionCommandExecutor.register(new ClearCommand(collectionManager));
        collectionCommandExecutor.register(new SaveCommand(collectionManager));
        collectionCommandExecutor.register(new ExitCommand());
        collectionCommandExecutor.register(new RemoveLowerCommand(collectionManager));
        collectionCommandExecutor.register(new ReplaceIfGreaterCommand(collectionManager));
        collectionCommandExecutor.register(new RemoveGreaterKeyCommand(collectionManager));
        collectionCommandExecutor.register(new FilterStartsWithNameCommand(collectionManager));
        collectionCommandExecutor.register(new PrintFieldAscendingEventCommand(collectionManager));
        collectionCommandExecutor.register(new PrintFieldDescendingTypeCommand(collectionManager));
        collectionCommandExecutor
                .getAllCommands()
                .forEach(
                        (name, command) -> {
                            if (command instanceof ObjectCollectionCommand)
                                commandList.add(
                                        new CommandAsList(
                                                command.getName(), command.getArgsCount(), true));
                            else
                                commandList.add(
                                        new CommandAsList(
                                                command.getName(), command.getArgsCount(), false));
                        });

        CommandExecutor<ServerCommand> serverCommandExecutor = new CommandExecutor<>();
        OutputWriter stdout = new Stdout();
        serverCommandExecutor.setOutputWriter(stdout);
        CommandParser commandParser = new CommandParser();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        InputReader inputReader =
                new InputReader(serverCommandExecutor, commandParser, bufferedReader);

        serverCommandExecutor.register(new ServerStartCommand(serverHandler));
        serverCommandExecutor.register(new ServerStatusCommand(serverHandler));
        serverCommandExecutor.register(new ServerStopCommand(serverHandler));
        serverCommandExecutor.register(new ServerHelpCommand(serverHandler, serverCommandExecutor));
        serverCommandExecutor.register(new ServerExitCommand(serverHandler));

        try {
            inputReader.process();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
