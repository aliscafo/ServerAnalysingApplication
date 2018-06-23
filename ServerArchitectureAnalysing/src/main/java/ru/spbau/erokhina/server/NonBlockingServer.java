package ru.spbau.erokhina.server;

import ru.spbau.erokhina.proto.MyArrayProtos;
import ru.spbau.erokhina.common.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonBlockingServer {
    private ServerSocketChannel serverSocketChannel;

    public NonBlockingServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(16202));
        } catch (IOException e) {
            System.err.println("Unable to open channel.");
            shutdown();
        }
    }

    private boolean isFinished = false;

    private ExecutorService threadPool = Executors.newFixedThreadPool(4);
    private ReadHandler readHandler = new ReadHandler();
    private WriteHandler writeHandler = new WriteHandler();

    public void start() {
        try {
            new Thread(readHandler).start();
            new Thread(writeHandler).start();

            while (!isFinished) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                readHandler.add(new ClientInstance(socketChannel));
                readHandler.handle();
            }

            shutdown();
        } catch (IOException e) {
            System.err.println("Unable to receive client.");
            shutdown();
        }
    }

    public void shutdown() {
        isFinished = true;
        threadPool.shutdown();
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            System.err.println("Unable to close channel.");
        }
    }

    private class AbstractHandler {
        protected ConcurrentLinkedQueue<ClientInstance> queue = new ConcurrentLinkedQueue<>();
        protected Selector selector;

        AbstractHandler() {
            try {
                selector = Selector.open();
            } catch (IOException e) {
                System.err.println("Unable to start selector.");
                shutdown();
            }
        }

        public void add(ClientInstance clientInstance) {
            queue.add(clientInstance);
        }

        public void handle() {
            selector.wakeup();
        }

        protected void registerClientsInSelector(int operation) {
            while (!queue.isEmpty()) {
                ClientInstance client = queue.poll();
                SocketChannel socketChannel = client.getSocketChannel();
                try {
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, operation, client);
                } catch (IOException e) {
                    System.err.println("Registration in selector failed.");
                    shutdown();
                }
            }
        }

        protected Iterator<SelectionKey> getReady() {
            try {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    return null;
                }

                return selector.selectedKeys().iterator();
            } catch (IOException e) {
                System.err.println("Unable to select keys.");
                shutdown();
            }

            return null;
        }
    }

    private class ReadHandler extends AbstractHandler implements Runnable {
        @Override
        public void run() {
            while (!isFinished) {
                registerClientsInSelector(SelectionKey.OP_READ);
                Iterator<SelectionKey> keyIterator = getReady();
                if (keyIterator != null) {
                    try {
                        readAll(keyIterator);
                    } catch (IOException e) {
                        System.err.println("Unable to read message.");
                        shutdown();
                    }
                }
            }
        }

        private void readAll(Iterator<SelectionKey> keyIterator) throws IOException {
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                ClientInstance client = (ClientInstance) key.attachment();

                client.read();
                keyIterator.remove();
            }
        }
    }

    private class WriteHandler extends AbstractHandler implements Runnable {
        @Override
        public void run() {
            while (!isFinished) {
                registerClientsInSelector(SelectionKey.OP_WRITE);
                Iterator<SelectionKey> keyIterator = getReady();
                if (keyIterator != null) {
                    try {
                        writeAll(keyIterator);
                    } catch (IOException e) {
                        System.err.println("Unable to write message.");
                        shutdown();
                    }
                }
            }
        }

        private void writeAll(Iterator<SelectionKey> keyIterator) throws IOException {
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                ClientInstance client = (ClientInstance) key.attachment();

                client.write();
                keyIterator.remove();
            }
        }
    }

    private class ClientInstance {
        private SocketChannel socketChannel;
        private long QueryTime;
        private long SortTime;

        private final ByteBuffer readBuffer = ByteBuffer.allocate(10000000);
        private final ByteBuffer writeBuffer = ByteBuffer.allocate(10000000);

        boolean newMessage = true;
        private byte[] message;
        private boolean isReadSize = false;
        private int completed = 0;
        private boolean dataWritten = false;

        public ClientInstance(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
            writeBuffer.flip();
        }

        @SuppressWarnings("StatementWithEmptyBody")
        public void read() throws IOException {
            if (newMessage) {
                QueryTime = System.currentTimeMillis();
                newMessage = false;
            }

            while (socketChannel.read(readBuffer) > 0);
            readPartly();

            if (isReadSize && message.length == completed) {
                byte[] data = message;
                isReadSize = false;
                completed = 0;
                MyArrayProtos.MyArray arrayToSort = MyArrayProtos.MyArray.parseFrom(data);
                threadPool.submit(new SortTask(arrayToSort.getDataList(), this));
            }
        }

        private class SortTask implements Runnable {
            private List<Integer> array;
            private ClientInstance parent;

            SortTask(List<Integer> list, ClientInstance client) {
                this.array = list;
                this.parent = client;
            }

            @Override
            public void run() {
                SortTime = System.currentTimeMillis();
                List<Integer> sortedList = Utils.selectedSort(array);
                SortTime = System.currentTimeMillis() - SortTime;

                MyArrayProtos.MyArray data = MyArrayProtos.MyArray.newBuilder()
                        .addAllData(sortedList)
                        .build();

                synchronized (writeBuffer) {
                    writeBuffer.compact();
                    writeBuffer.putInt(data.toByteArray().length);
                    writeBuffer.put(data.toByteArray());
                    writeBuffer.flip();
                }

                dataWritten = false;
                writeHandler.add(parent);
                writeHandler.handle();
            }
        }

        private void readPartly() throws IOException {
            readBuffer.flip();

            if (!isReadSize && readBuffer.remaining() >= 4) {
                int size = readBuffer.getInt();
                if (size == 0) {
                    socketChannel.close();
                    return;
                }
                message = new byte[size];
                isReadSize = true;
            }

            while (isReadSize && readBuffer.remaining() > 0) {
                message[completed++] = readBuffer.get();
            }

            readBuffer.compact();
        }

        public void write() throws IOException {
            synchronized (writeBuffer) {
                if (writeBuffer.remaining() == 0) {
                    return;
                }
                int written = socketChannel.write(writeBuffer);
                if (written > 0 && writeBuffer.remaining() == 0) {
                    if (dataWritten) {
                        newMessage = true;
                        return;
                    }
                    dataWritten = true;
                    QueryTime = System.currentTimeMillis() - QueryTime;
                    synchronized (writeBuffer) {
                        writeBuffer.compact();
                        writeBuffer.putLong(SortTime);
                        writeBuffer.putLong(QueryTime);
                        writeBuffer.flip();
                    }
                }
            }
        }

        public SocketChannel getSocketChannel() {
            return socketChannel;
        }
    }

    public static void main(String[] args) {
        NonBlockingServer server = new NonBlockingServer();
        server.start();
    }
}