package ru.spbau.erokhina.application;

import ru.spbau.erokhina.client.Client;
import ru.spbau.erokhina.common.CurrentInfo;
import ru.spbau.erokhina.common.Statistics;
import ru.spbau.erokhina.controller.Controller;

import java.io.IOException;

public class RunClients {
    public void run() {
        for (long i = CurrentInfo.getBorderFrom(); i <= CurrentInfo.getBorderTo(); i += CurrentInfo.getStep()) {
            CurrentInfo.setChangingParameter(i);

            Thread[] threads = new Thread[(int) CurrentInfo.getNumberOfClients()];

            for (int j = 0; j < threads.length; j++) {
                try {
                    threads[j] = new Thread(new Client("localhost", CurrentInfo.getPort()));
                } catch (IOException e) {
                    System.err.println("Come of clients failed tests, testing broken.");
                    Controller.setTestingFailed();
                    return;
                }
            }

            for (int j = 0; j < threads.length; j++) {
                threads[j].start();
            }

            for (int j = 0; j < threads.length; j++) {
                try {
                    threads[j].join();
                } catch (InterruptedException e) {
                    System.err.println("Error while joining client threads: " + e.getMessage());
                    Controller.setTestingFailed();
                    return;
                }
            }

            Statistics.getInstance().foldCurStatistics(i);
        }
    }

}
