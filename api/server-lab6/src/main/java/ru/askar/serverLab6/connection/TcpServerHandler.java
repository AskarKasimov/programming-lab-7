package ru.askar.serverLab6.connection;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import ru.askar.common.CommandAsList;
import ru.askar.common.CommandResponse;
import ru.askar.common.CommandToExecute;
import ru.askar.common.cli.CommandExecutor;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.serverLab6.collectionCommand.CollectionCommand;
import ru.askar.serverLab6.collectionCommand.ExitCommand;
import ru.askar.serverLab6.collectionCommand.ObjectCollectionCommand;

public class TcpServerHandler implements ServerHandler {
    private final CommandExecutor<CollectionCommand> collectionCommandExecutor;
    private final ArrayList<CommandAsList> commandList;
    private final Map<SocketChannel, ConcurrentLinkedQueue<Object>> clientOutputQueues =
            new ConcurrentHashMap<>();
    private int port = -1;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private boolean running = false;

    public TcpServerHandler(
            CommandExecutor<CollectionCommand> commandExecutor,
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
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(this.port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        clientOutputQueues.clear();
        running = true;

        new Thread(
                        () -> {
                            try {
                                while (running) {
                                    selector.select(100);
                                    processSelectedKeys();
                                    processOutputQueue();
                                }
                            } catch (Exception e) {
                                if (running) {
                                    System.out.println(
                                            "Ошибка в потоке хэндлера: " + e.getMessage());
                                } // а иначе тупо сервер оказался закрыт извне)))
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
        clientOutputQueues.put(clientChannel, new ConcurrentLinkedQueue<>());
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
                processCommand(channel, command);
            }
        } catch (Exception e) {
            System.out.println("Ошибка обработки команды: " + e.getMessage());
            sendMessage(channel, new CommandResponse(CommandResponseCode.ERROR, e.getMessage()));
        }
    }

    private void processCommand(SocketChannel channel, CommandToExecute command) {
        try {
            CollectionCommand calledCommand = collectionCommandExecutor.getCommand(command.name());
            if (calledCommand != null) {
                if (calledCommand instanceof ExitCommand) { // отлов намеренного дисконнекта клиента
                    handleDisconnect(channel.keyFor(selector), channel);
                    return;
                }
                if (calledCommand instanceof ObjectCollectionCommand) {
                    ((ObjectCollectionCommand) calledCommand).setObject(command.object());
                }
                CommandResponse response = calledCommand.execute(command.args());
                sendMessage(channel, response);
            } else {
                sendMessage(
                        channel,
                        new CommandResponse(CommandResponseCode.ERROR, "Команда не найдена"));
            }
        } catch (Exception e) {
            sendMessage(channel, new CommandResponse(CommandResponseCode.ERROR, e.getMessage()));
        }
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

        clientOutputQueues.remove(channel);
        System.out.println("Клиент отключен: " + clientAddress);
    }

    private void processOutputQueue() {
        Iterator<Map.Entry<SocketChannel, ConcurrentLinkedQueue<Object>>> iterator =
                clientOutputQueues.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<SocketChannel, ConcurrentLinkedQueue<Object>> entry = iterator.next();
            SocketChannel channel = entry.getKey();
            ConcurrentLinkedQueue<Object> queue = entry.getValue();

            try {
                if (!channel.isOpen()) {
                    iterator.remove();
                    continue;
                }

                while (!queue.isEmpty()) {
                    Object message = queue.poll();
                    ByteBuffer data = serialize(message);
                    ByteBuffer header = ByteBuffer.allocate(4).putInt(data.limit()).flip();

                    if (channel.write(new ByteBuffer[] {header, data}) == 0) {
                        queue.offer(message);
                        break;
                    }
                }
            } catch (IOException | CancelledKeyException e) {
                iterator.remove();
                try {
                    channel.close();
                } catch (IOException ex) {
                    // Игнорируем ошибку закрытия
                }
            }
        }
    }

    @Override
    public void sendMessage(SocketChannel channel, Object message) {
        ConcurrentLinkedQueue<Object> queue = clientOutputQueues.get(channel);
        if (queue != null) {
            queue.add(message);
        }
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
        running = false;
        try {
            for (SocketChannel channel : clientOutputQueues.keySet()) {
                try {
                    if (channel.isOpen()) {
                        channel.close();
                    }
                } catch (IOException e) {
                    // Игнорируем ошибку закрытия
                }
            }
            clientOutputQueues.clear();

            if (serverChannel != null) {
                serverChannel.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии ресурсов: " + e.getMessage());
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
