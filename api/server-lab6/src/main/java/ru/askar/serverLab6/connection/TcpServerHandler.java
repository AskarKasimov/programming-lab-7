package ru.askar.serverLab6.connection;

import ru.askar.common.CommandAsList;
import ru.askar.common.CommandResponse;
import ru.askar.common.CommandToExecute;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.ClientDisconnectException;
import ru.askar.serverLab6.CollectionCommandExecutor;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServerHandler implements ServerHandler {
    private final CollectionCommandExecutor collectionCommandExecutor;
    private final ArrayList<CommandAsList> commandList;
    private final ExecutorService requestReaderExecutor = Executors.newCachedThreadPool();
    private final ExecutorService requestProcessorExecutor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ExecutorService responseSenderExecutor = Executors.newCachedThreadPool();
    private int port = -1;
    private Selector selector;
    private boolean running = false;

    public TcpServerHandler(
            CollectionCommandExecutor commandExecutor,
            ArrayList<CommandAsList> commandList) {
        this.collectionCommandExecutor = commandExecutor;
        this.commandList = commandList;
    }

    @Override
    public void start() throws IOException {
        if (port == -1) {
            throw new IllegalStateException("Порт не задан");
        }
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(this.port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        running = true;

        new Thread(
                () -> {
                    try {
                        while (running) {
                            selector.select(100);
                            processSelectedKeys();
                        }
                    } catch (Exception e) {
                        if (running) {
                            System.out.println("Ошибка в потоке хэндлера: " + e.getMessage());
                        }
                    } finally {
                        closeResources();
                    }
                })
                .start();
    }

    private void processSelectedKeys() throws IOException {
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> iter = keys.iterator();

        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            iter.remove();

            if (!key.isValid()) continue;

            if (key.isAcceptable()) {
                handleAccept(key);
            } else if (key.isReadable()) {
                handleRead(key);
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        sendMessage(clientChannel, commandList);
        System.out.println("Клиент подключен: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buf = (ByteBuffer) key.attachment();

        try {
            if (buf == null) {
                buf = ByteBuffer.allocate(4);
                key.attach(buf);
            }

            int read = channel.read(buf);
            if (read == -1) {
                handleDisconnect(key, channel);
                return;
            }

            if (!buf.hasRemaining()) {
                buf.flip();

                if (buf.capacity() == 4) {
                    int size = buf.getInt();
                    key.attach(ByteBuffer.allocate(size));
                } else {
                    processReceivedData(channel, buf);
                    key.attach(null);
                }
            }
        } catch (IOException | CancelledKeyException e) {
            handleDisconnect(key, channel);
        }
    }

    private void processReceivedData(SocketChannel channel, ByteBuffer buf) {
        try {
            Object dto = deserialize(buf);
            if (dto instanceof CommandToExecute command) {
                System.out.println(
                        "Получена команда " + command + " от " + channel.getRemoteAddress());
                requestReaderExecutor.execute(() -> processCommand(channel, command));
            }
        } catch (Exception e) {
            System.out.println("Ошибка обработки команды: " + e.getMessage());
            sendMessage(channel, new CommandResponse(CommandResponseCode.ERROR, e.getMessage()));
        }
    }

    private void processCommand(SocketChannel channel, CommandToExecute command) {
        requestProcessorExecutor.submit(() -> {
            try {
                collectionCommandExecutor.validateCommand(command.name(), command.args().length);
                CommandResponse response;
                response = collectionCommandExecutor.execute(command.name(), command.args(), command.object(), command.credentials());
                sendMessage(channel, response);
            } catch (ClientDisconnectException e) {
                handleDisconnect(channel.keyFor(selector), channel);
            } catch (Exception e) {
                sendMessage(channel, new CommandResponse(CommandResponseCode.ERROR, e.getMessage()));
            }
        });
    }

    private void handleDisconnect(SelectionKey key, SocketChannel channel) {
        String clientAddress = "unknown client";
        try {
            clientAddress = channel.getRemoteAddress().toString();
        } catch (IOException ignored) {
        }

        try {
            key.cancel();
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (IOException ex) {
            System.out.println("Ошибка закрытия канала: " + ex.getMessage());
        }
        System.out.println("Клиент отключен: " + clientAddress);
    }

    @Override
    public void sendMessage(SocketChannel channel, Object message) {
        responseSenderExecutor.execute(() -> {
            synchronized (channel) { // Синхронизация на уровне канала
                try {
                    ByteBuffer data = serialize(message);
                    ByteBuffer header = ByteBuffer.allocate(4);
                    header.putInt(data.limit());
                    header.flip();
                    channel.write(new ByteBuffer[]{header, data});
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private ByteBuffer serialize(Object dto) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(dto);
            return ByteBuffer.wrap(bos.toByteArray());
        }
    }

    private Object deserialize(ByteBuffer buffer) throws IOException {
        try (ObjectInputStream ois =
                     new ObjectInputStream(
                             new ByteArrayInputStream(buffer.array(), 0, buffer.limit()))) {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Ошибка десериализации: " + e.getMessage());
        }
    }

    private void closeResources() {
        if (!running) return;
        running = false;
        try {
            requestReaderExecutor.shutdown();
            requestProcessorExecutor.shutdown();
            responseSenderExecutor.shutdown();
            for (SelectionKey selectionKey : selector.keys()) {
                selectionKey.channel().close();
            }
            selector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        closeResources();
    }

    @Override
    public boolean getStatus() {
        return running;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }
}
