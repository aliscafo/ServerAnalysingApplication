package ru.spbau.erokhina.server;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.erokhina.proto.MyArrayProtos;
import ru.spbau.erokhina.common.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ThreadPerClientServer {
    private static final int serverPort = 16200;
    private ServerSocket serverSocket;
    private boolean flag;

    /**
     * Method for launching a server.
     */
    public void start() throws IOException {
        flag = true;
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (Exception e) {
            return;
        }

        new Thread(()->{
            try {
                while (flag) {
                    Socket socket = serverSocket.accept();
                    try {
                        new OneThreadedServer(socket);
                    } catch (IOException e) {
                        socket.close();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    /**
     * Method for stopping a server.
     */
    public void stopServer() {
        flag = false;
    }

    private class OneThreadedServer extends Thread {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        OneThreadedServer(Socket socket) throws IOException {
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            setDaemon(true);

            start();
        }

        public void run () {
            try {
                while (true) {
                    long startClientTime = System.currentTimeMillis();
                    int size = in.readInt();

                    if (size == 0) {
                        break;
                    }
                    byte[] bytes = new byte[size];
                    in.readFully(bytes);

                    MyArrayProtos.MyArray receivedArray = MyArrayProtos.MyArray.parseFrom(bytes);

                    long startQueryTime = System.currentTimeMillis();
                    List<Integer> list = receivedArray.getDataList();
                    List<Integer> sortedList = Utils.selectedSort(list);
                    long finishQueryTime = System.currentTimeMillis();

                    MyArrayProtos.MyArray myArray = MyArrayProtos.MyArray.newBuilder()
                            .addAllData(sortedList)
                            .build();

                    out.writeInt(myArray.toByteArray().length);
                    out.write(myArray.toByteArray());

                    long finishClientTime = System.currentTimeMillis();

                    out.writeLong(finishQueryTime - startQueryTime);
                    out.writeLong(finishClientTime - startClientTime);
                    out.flush();
                }

            } catch (InvalidProtocolBufferException e) {
                System.out.println("Invalid protocol exception: " + e.getMessage());
                stopServer();
            } catch (IOException e) {
                stopServer();
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                }
                catch (IOException e) {
                    System.err.println("Socket was not closed");
                }
            }
        }
    }

    /**
     * Method for launching server from console.
     */
    public static void main(String[] args) throws IOException {
        ThreadPerClientServer server = new ThreadPerClientServer();
        server.start();
    }
}
