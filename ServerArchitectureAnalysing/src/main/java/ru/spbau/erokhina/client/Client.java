package ru.spbau.erokhina.client;

import javafx.scene.control.Alert;
import ru.spbau.erokhina.common.CurrentInfo;
import ru.spbau.erokhina.controller.Controller;
import ru.spbau.erokhina.proto.MyArrayProtos;
import ru.spbau.erokhina.common.Statistics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Random rand = new Random();

    /**
     * Constructor for a Client.
     * @param host host address
     * @param port given port
     */
    public Client(String host, int port) throws IOException {
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
            socket = new Socket(address, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

        } catch (UnknownHostException e) {
            throw new IOException("Unable to connect to server, testing failed.");
        } catch (IOException e) {
            socket.close();
            throw new IOException("Unable to connect to server, testing failed.");
        }
    }

    private void sendRequestAndReceiveAnswer() throws IOException, InterruptedException {
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < CurrentInfo.getArraySize(); i++) {
            int number = rand.nextInt();
            list.add(number);
        }

        MyArrayProtos.MyArray myArray = MyArrayProtos.MyArray.newBuilder()
                .addAllData(list)
                .build();

        out.writeInt(myArray.toByteArray().length);
        out.write(myArray.toByteArray());
        out.flush();

        int size = in.readInt();
        byte[] bytes = new byte[size];
        in.readFully(bytes);

        long queryTimeMs = in.readLong();

        long clientTimeMs = in.readLong();

        Statistics.getInstance().addQueryTime(queryTimeMs);
        Statistics.getInstance().addClientTime(clientTimeMs);

        Thread.sleep(CurrentInfo.getDeltaInterval());
    }

    public void run() {
        long start = System.currentTimeMillis();

        for (int i = 0; i < CurrentInfo.getNumberOfQueries(); i++) {
            try {
                sendRequestAndReceiveAnswer();
            } catch (IOException | InterruptedException e) {
                showAlert("Something went wrong while processing the request.");
                Controller.setTestingFailed();
            }
        }

        try {
            out.writeInt(0);
        } catch (IOException e) {
            showAlert("Couldn't write symbol of ending the work of the client.");
            Controller.setTestingFailed();
        }

        long finish = System.currentTimeMillis();

        Statistics.getInstance().addAverageQueryTime((finish - start) / CurrentInfo.getNumberOfQueries());

        try {
            closeAll();
        } catch (IOException e) {
            showAlert("Couldn't close socket or streams.");
            Controller.setTestingFailed();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Method for closing socket and streams.
     */
    private void closeAll() throws IOException {
        socket.close();
        in.close();
        out.close();
    }
}


