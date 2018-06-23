package ru.spbau.erokhina.common;

import java.util.ArrayList;
import java.util.List;

public class Statistics {
    private List<Coordinate> queryTimeMs = new ArrayList<>();
    private List<Coordinate> clientTimeMs = new ArrayList<>();
    private List<Coordinate> averageQueryTimeMs = new ArrayList<>();

    private List<Long> curQueryTimeMs = new ArrayList<>();
    private List<Long> curClientTimeMs = new ArrayList<>();
    private List<Long> curAverageQueryTimeMs = new ArrayList<>();

    public synchronized void addQueryTime (long queryTime) {
        Long queryTimeLong = (Long) queryTime;
        curQueryTimeMs.add(queryTimeLong);
    }

    public synchronized void addClientTime (Long clientTime) {
        curClientTimeMs.add(clientTime);
    }

    public synchronized void addAverageQueryTime (Long queryTime) {
        curAverageQueryTimeMs.add(queryTime);
    }

    public int getSize() {
        return queryTimeMs.size();
    }

    public Coordinate getQueryTimeCoordinate(int i) {
        return queryTimeMs.get(i);
    }

    public Coordinate getClientTimeCoordinate(int i) {
        return clientTimeMs.get(i);
    }

    public Coordinate getAverageQueryTimeCoordinate(int i) {
        return averageQueryTimeMs.get(i);
    }

    public int getQueryTimeSize() {
        return queryTimeMs.size();
    }

    public int getClientTimeSize() {
        return clientTimeMs.size();
    }

    public int getAverageQueryTimeSize() {
        return averageQueryTimeMs.size();
    }

    public synchronized void foldCurStatistics(Long x) {
        Long sumQueryTimeMs = 0L;
        for (Long curTime : curQueryTimeMs) {
            sumQueryTimeMs += curTime;
        }
        queryTimeMs.add(new Coordinate(x, sumQueryTimeMs / curQueryTimeMs.size()));

        Long sumClientTimeMs = 0L;
        for (Long curTime : curClientTimeMs) {
            sumClientTimeMs += curTime;
        }
        clientTimeMs.add(new Coordinate(x, sumClientTimeMs / curClientTimeMs.size()));

        Long sumAverageQueryTimeMs = 0L;
        for (Long curTime : curAverageQueryTimeMs) {
            sumAverageQueryTimeMs += curTime;
        }
        averageQueryTimeMs.add(new Coordinate(x, sumAverageQueryTimeMs / curAverageQueryTimeMs.size()));

        curQueryTimeMs.clear();
        curClientTimeMs.clear();
        curAverageQueryTimeMs.clear();
    }

    public static class Coordinate {
        private long x, y;

        Coordinate(long x, long y) {
            this.x = x;
            this.y = y;
        }

        public long getX() {
            return x;
        }

        public long getY() {
            return y;
        }

    }

    public synchronized static Statistics getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static class SingletonHolder {
        static final Statistics INSTANCE = new Statistics();
    }
}
