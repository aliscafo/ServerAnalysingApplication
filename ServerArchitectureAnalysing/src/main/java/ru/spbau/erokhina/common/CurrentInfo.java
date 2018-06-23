package ru.spbau.erokhina.common;

public class CurrentInfo {
    static private long arraySize = 10000;
    static private long numberOfClients = 5;
    static private long deltaInterval = 20;
    static private long numberOfQueries = 5;
    static private ServerArchitecture serverArchitecture = ServerArchitecture.THREAD_PER_CLIENT;
    static private Parameter parameter = Parameter.INTERVAL_DELTA;
    static private long borderFrom = 10;
    static private long borderTo = 1000;
    static private long step = 30;
    static private Metrics curChart = Metrics.QUERY_TIME;
    static private int port;

    public static ServerArchitecture getServerArchitecture() {
        return serverArchitecture;
    }

    public static void setServerArchitecture(ServerArchitecture serverArchitecture) {
        CurrentInfo.serverArchitecture = serverArchitecture;
    }

    public static Parameter getParameter() {
        return parameter;
    }

    public static void setParameter(Parameter parameter) {
        CurrentInfo.parameter = parameter;
    }

    public static long getBorderFrom() {
        return borderFrom;
    }

    public static void setBorderFrom(long borderFrom) {
        CurrentInfo.borderFrom = borderFrom;
    }

    public static long getBorderTo() {
        return borderTo;
    }

    public static void setBorderTo(long borderTo) {
        CurrentInfo.borderTo = borderTo;
    }

    public static long getStep() {
        return step;
    }

    public static void setStep(long step) {
        CurrentInfo.step = step;
    }

    public static void setChangingParameter(long i) {
        switch (getParameter()) {
            case ARRAY_SIZE_N:
                setArraySize(i);
                break;
            case CLIENTS_NUMBER_M:
                setNumberOfClients(i);
                break;
            case INTERVAL_DELTA:
                setDeltaInterval(i);
                break;
        }
    }

    public static Metrics getCurChart() {
        return curChart;
    }

    public static void setCurChart(Metrics curChart) {
        CurrentInfo.curChart = curChart;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        CurrentInfo.port = port;
    }

    public enum ServerArchitecture {THREAD_PER_CLIENT, WITH_SINGLE_THREAD_EXECUTOR, NON_BLOCKING}
    public enum Parameter {ARRAY_SIZE_N, CLIENTS_NUMBER_M, INTERVAL_DELTA}
    public enum Metrics {QUERY_TIME, CLIENT_TIME, AVERAGE_QUERY_TIME}

    public static long getArraySize() {
        return arraySize;
    }

    public static void setArraySize(long arraySize) {
        CurrentInfo.arraySize = arraySize;
    }

    public static long getNumberOfQueries() {
        return numberOfQueries;
    }

    public static void setNumberOfQueries(long numberOfQueries) {
        CurrentInfo.numberOfQueries = numberOfQueries;
    }

    public static long getNumberOfClients() {
        return numberOfClients;
    }

    public static void setNumberOfClients(long numberOfClients) {
        CurrentInfo.numberOfClients = numberOfClients;
    }

    public static long getDeltaInterval() {
        return deltaInterval;
    }

    public static void setDeltaInterval(long deltaInterval) {
        CurrentInfo.deltaInterval = deltaInterval;
    }

    public static String stringByServerArchitecture(ServerArchitecture serverArchitecture) {
        switch (serverArchitecture) {
            case THREAD_PER_CLIENT:
                return "One thread per client";
            case WITH_SINGLE_THREAD_EXECUTOR:
                return "With SingleThreadExecutor";
            case NON_BLOCKING:
                return "Non-blocking";
            default:
                return "";
        }
    }

    public static String stringByParameter(Parameter parameter) {
        switch (parameter) {
            case ARRAY_SIZE_N:
                return "N (array size)";
            case CLIENTS_NUMBER_M:
                return "M (number of clients)";
            case INTERVAL_DELTA:
                return "∆ (interval between queries)";
            default:
                return "";
        }
    }
}
