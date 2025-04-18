package ru.askar.serverLab6;

import ru.askar.common.CommandAsList;
import ru.askar.common.cli.CommandExecutor;
import ru.askar.common.cli.CommandParser;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.cli.input.InputReader;
import ru.askar.common.cli.output.OutputWriter;
import ru.askar.common.cli.output.Stdout;
import ru.askar.serverLab6.collection.CollectionManager;
import ru.askar.serverLab6.collection.DataReader;
import ru.askar.serverLab6.collection.JsonReader;
import ru.askar.serverLab6.collectionCommand.*;
import ru.askar.serverLab6.connection.ServerHandler;
import ru.askar.serverLab6.connection.TcpServerHandler;
import ru.askar.serverLab6.serverCommand.*;

import java.io.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        String filePath = System.getenv("COLLECTION_PATH");
        if (filePath == null) {
            System.out.println(
                    CommandResponseCode.ERROR.getColoredMessage(
                            "Переменная окружения COLLECTION_PATH не установлена"));
            return;
        }
        System.out.println(CommandResponseCode.INFO.getColoredMessage("Файл: " + filePath));
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException | SecurityException e) {
            System.out.println(
                    CommandResponseCode.ERROR.getColoredMessage(
                            "Файл не удаётся прочитать: " + e.getMessage()));
        }

        DataReader dataReader = new JsonReader(filePath, bufferedInputStream);
        if (bufferedInputStream == null) {
            dataReader = null;
        }

        CollectionManager collectionManager = null;
        try {
            collectionManager = new CollectionManager(dataReader);
        } catch (Exception e) {
            System.out.println(CommandResponseCode.ERROR.getColoredMessage(e.getMessage()));
        } finally {
            try {
                if (bufferedInputStream != null) bufferedInputStream.close();
            } catch (IOException e) {
                System.out.println(
                        CommandResponseCode.ERROR.getColoredMessage(
                                "Ошибка при закрытии файла: " + e.getMessage()));
            }
        }
        if (collectionManager == null) {
            try {
                collectionManager = new CollectionManager(null);
            } catch (Exception e) {
                // ignored
            }
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
