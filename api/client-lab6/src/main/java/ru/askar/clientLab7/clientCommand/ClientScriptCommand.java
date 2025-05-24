package ru.askar.clientLab7.clientCommand;

import ru.askar.clientLab7.connection.ClientHandler;
import ru.askar.common.CommandResponse;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.cli.input.InputReader;

import java.io.*;

public class ClientScriptCommand extends ClientCommand {
    private final InputReader inputReader;

    public ClientScriptCommand(ClientHandler clientHandler, InputReader inputReader) {
        super(
                "execute_script",
                1,
                "execute_script filename - считать и исполнить скрипт из указанного файла",
                clientHandler);
        this.inputReader = inputReader;
    }

    @Override
    public CommandResponse execute(String[] args) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(args[0]);
        } catch (FileNotFoundException e) {
            return new CommandResponse(CommandResponseCode.ERROR, "Файл не найден");
        }
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        BufferedReader lastBufferedReader = inputReader.getBufferedReader();
        boolean lastScriptMode = inputReader.isScriptMode();
        inputReader.setBufferedReader(bufferedReader);
        inputReader.setScriptMode(true);
        try {
            inputReader.process();
        } catch (IOException e) {
            return new CommandResponse(CommandResponseCode.ERROR, e.getMessage());
        }
        inputReader.setScriptMode(lastScriptMode);
        inputReader.setBufferedReader(lastBufferedReader);
        return new CommandResponse(CommandResponseCode.SUCCESS, "Скрипт успешно выполнен");
    }
}
