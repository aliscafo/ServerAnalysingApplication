package ru.spbau.erokhina.application;

import ru.spbau.erokhina.client.Client;
import ru.spbau.erokhina.common.CurrentInfo;
import ru.spbau.erokhina.common.Statistics;

import java.io.IOException;

public class RunClients {
    public void run() throws IOException {
        for (long i = CurrentInfo.getBorderFrom(); i <= CurrentInfo.getBorderTo(); i += CurrentInfo.getStep()) {
            CurrentInfo.setChangingParameter(i);

            Thread[] threads = new Thread[(int) CurrentInfo.getNumberOfClients()];

            for (int j = 0; j < threads.length; j++) {
                threads[j] = new Thread(new Client("localhost", CurrentInfo.getPort()));
            }

            for (int j = 0; j < threads.length; j++) {
                threads[j].start();
            }

            for (int j = 0; j < threads.length; j++) {
                try {
                    threads[j].join();
                } catch (InterruptedException e) {
                    System.out.println("Error while joining client threads: " + e.getMessage());
                }
            }

            Statistics.getInstance().foldCurStatistics(i);
        }
    }

}
