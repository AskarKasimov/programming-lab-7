package ru.askar.clientLab6.connection;

import ru.askar.clientLab6.NeedToReconnectException;
import ru.askar.clientLab6.clientCommand.ClientCommand;
import ru.askar.clientLab6.clientCommand.ClientGenericCommand;
import ru.askar.common.CommandAsList;
import ru.askar.common.CommandResponse;
import ru.askar.common.Credentials;
import ru.askar.common.cli.CommandExecutor;
import ru.askar.common.cli.CommandParser;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.cli.input.InputReader;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TcpClientHandler implements ClientHandler {
    private final InputReader<ClientCommand> inputReader; // основной InputReader
    private final CommandExecutor<ClientCommand> commandExecutor;
    private final ConcurrentLinkedQueue<Object> outputQueue = new ConcurrentLinkedQueue<>();
    private final int maxDepth = 3;
    private final List<ClientCommand> originalCommands = new ArrayList<>();
    private String host = "";
    private int port = -1;
    private Selector selector;
    private SocketChannel channel;
    private volatile boolean running = false;
    private int depth = 0;
    // Для вложенного режима
    private InputReader<ClientCommand> nestedInputReader = null;
    private Credentials credentials;

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public TcpClientHandler(
            InputReader<ClientCommand> inputReader, CommandExecutor<ClientCommand> commandExecutor) {
        this.inputReader = inputReader;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void start() throws IOException, NeedToReconnectException {
        if (host.isEmpty() || port == -1) {
            throw new IllegalStateException("Нужно указать хост и порт");
        }
        selector = Selector.open();
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        try {
            channel.connect(new InetSocketAddress(host, port));
        } catch (UnresolvedAddressException e) {
            throw new IOException("Некорректный адрес: " + host);
        } catch (SecurityException e) {
            throw new IOException("Ошибка безопасности: " + e.getMessage());
        } catch (IllegalArgumentException | IOException | IllegalStateException e) {
            throw new IOException(
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
        channel.register(selector, SelectionKey.OP_CONNECT);
        outputQueue.clear();
        running = true;

        originalCommands.clear();
        originalCommands.addAll(commandExecutor.getAllCommands().values());
        try {
            while (running) {
                selector.select(100);
                processSelectedKeys();
                processOutputQueue();
            }
        } catch (IOException e) {
            //
        } finally {
            closeResources();
        }
    }

    private void processSelectedKeys() throws IOException, NeedToReconnectException {
        Set<SelectionKey> keys;
        try {
            keys = selector.selectedKeys();
        } catch (ClosedSelectorException e) {
            throw new IOException(e);
        }
        Iterator<SelectionKey> iter = keys.iterator();

        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            iter.remove();

            if (!key.isValid()) continue;

            if (key.isConnectable()) {
                handleConnect(key);
            } else if (key.isReadable()) {
                handleRead(key);
            }
        }
    }

    private void handleConnect(SelectionKey key) throws NeedToReconnectException {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.finishConnect()) {
                channel.register(selector, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            retryConnection();
            handleDisconnect();
        }
    }

    private void handleRead(SelectionKey key) throws NeedToReconnectException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buf = (ByteBuffer) key.attachment();

        try {
            if (buf == null) {
                buf = ByteBuffer.allocate(4);
                key.attach(buf);
            }

            int read = channel.read(buf);
            if (read == -1) {
                handleDisconnect();
                return;
            }

            if (!buf.hasRemaining()) {
                buf.flip();

                // Проверка размера данных
                if (buf.capacity() == 4) {
                    int size = buf.getInt();
                    if (size <= 0 || size > 10_000_000) { // Максимальный размер 10MB
                        throw new IOException("Некорректный размер данных: " + size);
                    }
                    key.attach(ByteBuffer.allocate(size));
                } else {
                    Object dto = deserialize(buf);
                    if (dto instanceof ArrayList<?> list
                            && !list.isEmpty()
                            && list.get(0) instanceof CommandAsList) {
                        @SuppressWarnings("unchecked")
                        ArrayList<CommandAsList> commandsAsList = (ArrayList<CommandAsList>) list;

                        if (nestedInputReader == null) {
                            commandExecutor.clearCommands();
                            nestedInputReader =
                                    new InputReader<>(
                                            commandExecutor,
                                            new CommandParser(),
                                            inputReader.getBufferedReader());
                            commandExecutor
                                    .getOutputWriter()
                                    .write(
                                            CommandResponseCode.SUCCESS.getColoredMessage(
                                                    "Вход в режим полученных команд сервера"));
                        } else {
                            commandExecutor.clearCommands();
                        }
                        for (CommandAsList commandAsList : commandsAsList) {
                            commandExecutor.register(
                                    new ClientGenericCommand(
                                            nestedInputReader,
                                            commandAsList,
                                            this,
                                            commandExecutor.getOutputWriter(),
                                            credentials));
                        }
                    } else if (dto instanceof CommandResponse commandResponse) {
                        commandExecutor
                                .getOutputWriter()
                                .write(
                                        commandResponse
                                                .code()
                                                .getColoredMessage(commandResponse.response()));
                    } else {
                        System.out.println("Клиент не смог распознать сообщение");
                    }
                    key.attach(null);
                }
            }
        } catch (IOException | IllegalArgumentException | ClassNotFoundException e) {
            System.err.println("Сетевая ошибка");
            retryConnection();
            handleDisconnect();
        }
    }

    private void retryConnection() throws NeedToReconnectException {
        commandExecutor
                .getOutputWriter()
                .write(
                        CommandResponseCode.ERROR.getColoredMessage(
                                "Потеряно соединение с сервером"));
        if (depth < maxDepth) throw new NeedToReconnectException(++depth);
        else
            commandExecutor
                    .getOutputWriter()
                    .write(CommandResponseCode.ERROR.getColoredMessage("Попытки кончились"));
    }

    private void handleDisconnect() {
        running = false;
        depth = 0;
        if (nestedInputReader != null) {
            commandExecutor
                    .getOutputWriter()
                    .write(
                            CommandResponseCode.WARNING.getColoredMessage(
                                    "Отключение от сервера. Возврат к локальному режиму."));
            commandExecutor.clearCommands();
            originalCommands.forEach(command -> commandExecutor.register(command));
            nestedInputReader = null;
        }

        closeResources();
    }

    private void processOutputQueue() throws NeedToReconnectException {
        try {
            if (channel != null && channel.isConnected()) {
                while (!outputQueue.isEmpty()) {
                    Object message = outputQueue.poll();
                    ByteBuffer data = serialize(message);
                    ByteBuffer header = ByteBuffer.allocate(4);
                    header.putInt(data.limit());
                    header.flip();
                    channel.write(new ByteBuffer[]{header, data});
                }
            }
        } catch (IOException | CancelledKeyException e) {
            System.out.println("Ошибка при отправке данных: " + e.getMessage());
            retryConnection();
            handleDisconnect();
        }
    }

    @Override
    public void sendMessage(Object message) {
        outputQueue.add(message);
    }

    private ByteBuffer serialize(Object dto) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(dto);
            return ByteBuffer.wrap(bos.toByteArray());
        }
    }

    private Object deserialize(ByteBuffer buffer) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois =
                     new ObjectInputStream(
                             new ByteArrayInputStream(buffer.array(), 0, buffer.limit()))) {
            return ois.readObject();
        }
    }

    private void closeResources() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии ресурсов: " + e.getMessage());
        } finally {
            running = false;
            outputQueue.clear();
        }
    }

    @Override
    public void stop() {
        handleDisconnect();
    }

    @Override
    public boolean getRunning() {
        return running;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }
}
