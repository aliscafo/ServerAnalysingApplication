package ru.spbau.erokhina.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.spbau.erokhina.application.RunClients;
import ru.spbau.erokhina.common.CurrentInfo;
import ru.spbau.erokhina.common.Statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

public class Controller {
    private static boolean testingFailed = false;

    public static boolean isTestingFailed() {
        return testingFailed;
    }

    public static void setTestingFailed() {
        testingFailed = true;
    }

    public void okButtonOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();

        ChoiceBox<String> choiceBoxArchitecture = (ChoiceBox<String>) stage.getScene().lookup("#choose_architecture");
        switch (choiceBoxArchitecture.getValue()) {
            case "One thread per client":
                CurrentInfo.setServerArchitecture(CurrentInfo.ServerArchitecture.THREAD_PER_CLIENT);
                break;
            case "With SingleThreadExecutor":
                CurrentInfo.setServerArchitecture(CurrentInfo.ServerArchitecture.WITH_SINGLE_THREAD_EXECUTOR);
                break;
            case "Non-blocking":
                CurrentInfo.setServerArchitecture(CurrentInfo.ServerArchitecture.NON_BLOCKING);
                break;
        }

        TextField fieldNumberOfQueries = (TextField) stage.getScene().lookup("#number_of_queries");
        if (!Objects.equals(fieldNumberOfQueries.getText(), "")) {
            CurrentInfo.setNumberOfQueries(Integer.parseInt(fieldNumberOfQueries.getText()));
        }

        ChoiceBox<String> choiceBoxParameter = (ChoiceBox<String>) stage.getScene().lookup("#parameter");
        switch (choiceBoxParameter.getValue()) {
            case "N (array size)":
                CurrentInfo.setParameter(CurrentInfo.Parameter.ARRAY_SIZE_N);
                break;
            case "M (number of clients)":
                CurrentInfo.setParameter(CurrentInfo.Parameter.CLIENTS_NUMBER_M);
                break;
            case "∆ (interval between queries)":
                CurrentInfo.setParameter(CurrentInfo.Parameter.INTERVAL_DELTA);
                break;
        }

        TextField fieldBorderFrom = (TextField) stage.getScene().lookup("#from");
        if (!Objects.equals(fieldBorderFrom.getText(), "")) {
            CurrentInfo.setBorderFrom(Long.parseLong(fieldBorderFrom.getText()));
        }

        TextField fieldBorderTo = (TextField) stage.getScene().lookup("#to");
        if (!Objects.equals(fieldBorderTo.getText(), "")) {
            CurrentInfo.setBorderTo(Long.parseLong(fieldBorderTo.getText()));
        }

        TextField fieldStep = (TextField) stage.getScene().lookup("#step");
        if (!Objects.equals(fieldStep.getText(), "")) {
            CurrentInfo.setStep(Long.parseLong(fieldStep.getText()));
        }

        TextField fieldArraySize = (TextField) stage.getScene().lookup("#array_size");
        if (!Objects.equals(fieldArraySize.getText(), "")) {
            CurrentInfo.setArraySize(Integer.parseInt(fieldArraySize.getText()));
        }

        TextField fieldNumberOfClients = (TextField) stage.getScene().lookup("#number_of_clients");
        if (!Objects.equals(fieldNumberOfClients.getText(), "")) {
            CurrentInfo.setNumberOfClients(Integer.parseInt(fieldNumberOfClients.getText()));
        }

        TextField fieldDelta = (TextField) stage.getScene().lookup("#delta");
        if (!Objects.equals(fieldDelta.getText(), "")) {
            CurrentInfo.setDeltaInterval(Integer.parseInt(fieldDelta.getText()));
        }

        switch (CurrentInfo.getServerArchitecture()) {
            case THREAD_PER_CLIENT:
                CurrentInfo.setPort(16200);
                break;
            case WITH_SINGLE_THREAD_EXECUTOR:
                CurrentInfo.setPort(16201);
                break;
            case NON_BLOCKING:
                CurrentInfo.setPort(16202);
                break;
        }

        RunClients runClients = new RunClients();
        runClients.run();

        if (isTestingFailed()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error during testing");
            alert.setContentText("Fatal error during testing occurred: test packages weren't completed, testing failed.");
            alert.showAndWait();
            Platform.exit();
            return;
        }

        writeDataToFiles();

        loadQueryTimeChart(stage);
    }

    private void writeDataToFiles() {
        try(FileWriter writer = new FileWriter("queryTime.txt", false)) {
            writeHeader(writer);

            for (int i = 0; i < Statistics.getInstance().getQueryTimeSize(); i++) {
                String number = Long.toString(Statistics.getInstance().getQueryTimeCoordinate(i).getY());
                writer.write(number + "\n");
            }

            writer.flush();
        }
        catch(IOException e){
            System.out.println("Error while writing data to files: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error during write results");
            alert.setContentText("Application was unable to write results of testing.");
            alert.showAndWait();
        }

        try(FileWriter writer = new FileWriter("clientTime.txt", false)) {
            writeHeader(writer);

            for (int i = 0; i < Statistics.getInstance().getClientTimeSize(); i++) {
                String number = Long.toString(Statistics.getInstance().getClientTimeCoordinate(i).getY());
                writer.write(number + "\n");
            }

            writer.flush();
        }
        catch(IOException e){
            System.out.println("Error while writing data to files: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error during write results.");
            alert.setContentText("Application was unable to write results of testing.");
            alert.showAndWait();
        }

        try(FileWriter writer = new FileWriter("averageQueryTime.txt", false)) {
            writeHeader(writer);

            for (int i = 0; i < Statistics.getInstance().getAverageQueryTimeSize(); i++) {
                String number = Long.toString(Statistics.getInstance().getAverageQueryTimeCoordinate(i).getY());
                writer.write(number + "\n");
            }

            writer.flush();
        }
        catch(IOException e){
            System.out.println("Error while writing data to files: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error during write results.");
            alert.setContentText("Application was unable to write results of testing.");
            alert.showAndWait();
        }
    }

    private void writeHeader(Writer writer) throws IOException {
        writer.write("Server Architecture: " +
                CurrentInfo.stringByServerArchitecture(CurrentInfo.getServerArchitecture()) + "\n");
        writer.write("X (number of queries): " + CurrentInfo.getNumberOfClients() + "\n");
        writer.write("Parameter: " + CurrentInfo.stringByParameter(CurrentInfo.getParameter()) + "\n");
        writer.write("From: " + CurrentInfo.getBorderFrom() + "\n");
        writer.write("To: " + CurrentInfo.getBorderTo() + "\n");
        writer.write("Step: " + CurrentInfo.getStep() + "\n");
        if (CurrentInfo.getParameter() != CurrentInfo.Parameter.ARRAY_SIZE_N) {
            writer.write("Constant N (array size): " + CurrentInfo.getArraySize() + "\n");
        }
        if (CurrentInfo.getParameter() != CurrentInfo.Parameter.CLIENTS_NUMBER_M) {
            writer.write("Constant M (number of clients): " + CurrentInfo.getNumberOfClients() + "\n");
        }
        if (CurrentInfo.getParameter() != CurrentInfo.Parameter.INTERVAL_DELTA) {
            writer.write("Constant ∆ (interval between queries): " + CurrentInfo.getDeltaInterval() + "\n");
        }
        writer.write("\n");
    }

    public void rightArrowOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();

        switch (CurrentInfo.getCurChart()) {
            case QUERY_TIME:
                CurrentInfo.setCurChart(CurrentInfo.Metrics.CLIENT_TIME);
                loadClientTimeChart(stage);
                break;
            case CLIENT_TIME:
                CurrentInfo.setCurChart(CurrentInfo.Metrics.AVERAGE_QUERY_TIME);
                loadAverageQueryTimeChart(stage);
                break;
            case AVERAGE_QUERY_TIME:
                break;
        }
    }

    public void leftArrowOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();

        switch (CurrentInfo.getCurChart()) {
            case QUERY_TIME:
                break;
            case CLIENT_TIME:
                CurrentInfo.setCurChart(CurrentInfo.Metrics.QUERY_TIME);
                loadQueryTimeChart(stage);
                break;
            case AVERAGE_QUERY_TIME:
                CurrentInfo.setCurChart(CurrentInfo.Metrics.CLIENT_TIME);
                loadClientTimeChart(stage);
                break;
        }
    }

    private void loadQueryTimeChart (Stage stage) {
        Parent panel = null;
        try {
            panel = FXMLLoader.load(Controller.class.getResource("/chart.fxml"));
        } catch (IOException e) {
            System.out.println("Unable to load application window, stop application...");
            Platform.exit();
        }
        Scene scene = new Scene(panel, 600, 590);

        LineChart<?,?> lineChart = (LineChart<?, ?>) scene.lookup("#lineChart");

        XYChart.Series series = new XYChart.Series();

        for (int i = 0; i < Statistics.getInstance().getQueryTimeSize(); i++) {
            series.getData().add(new XYChart.Data(Statistics.getInstance().getQueryTimeCoordinate(i).getX(),
                    Statistics.getInstance().getQueryTimeCoordinate(i).getY()));
        }

        lineChart.getData().addAll(series);

        stage.setScene(scene);
        stage.show();
    }

    private void loadClientTimeChart (Stage stage) {
        Parent panel = null;
        try {
            panel = FXMLLoader.load(Controller.class.getResource("/chart.fxml"));
        } catch (IOException e) {
            System.out.println("Unable to load application window, stop application...");
            Platform.exit();
        }
        Scene scene = new Scene(panel, 600, 590);

        LineChart<?,?> lineChart = (LineChart<?, ?>) scene.lookup("#lineChart");

        XYChart.Series series = new XYChart.Series();

        for (int i = 0; i < Statistics.getInstance().getQueryTimeSize(); i++) {
            series.getData().add(new XYChart.Data(Statistics.getInstance().getClientTimeCoordinate(i).getX(),
                    Statistics.getInstance().getClientTimeCoordinate(i).getY()));
        }

        lineChart.getData().addAll(series);

        stage.setScene(scene);
        stage.show();
    }

    private void loadAverageQueryTimeChart (Stage stage) {
        Parent panel = null;
        try {
            panel = FXMLLoader.load(Controller.class.getResource("/chart.fxml"));
        } catch (IOException e) {
            System.out.println("Unable to load application window, stop application...");
            Platform.exit();
        }
        Scene scene = new Scene(panel, 600, 590);

        LineChart<?,?> lineChart = (LineChart<?, ?>) scene.lookup("#lineChart");

        XYChart.Series series = new XYChart.Series();

        for (int i = 0; i < Statistics.getInstance().getQueryTimeSize(); i++) {
            series.getData().add(new XYChart.Data(Statistics.getInstance().getAverageQueryTimeCoordinate(i).getX(),
                    Statistics.getInstance().getAverageQueryTimeCoordinate(i).getY()));
        }

        lineChart.getData().addAll(series);

        stage.setScene(scene);
        stage.show();
    }
}
