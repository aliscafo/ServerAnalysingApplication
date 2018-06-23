package ru.spbau.erokhina.application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerAnalysingApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setMenu(primaryStage);

        primaryStage.setTitle("Server Analysing Application");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void setMenu(Stage primaryStage) throws IOException {
        Parent panel = FXMLLoader.load(ServerAnalysingApplication.class.getResource("/enter_host.fxml"));

        Scene scene = new Scene(panel, 600, 590);

        ChoiceBox<String> choiceBoxArchitecture = (ChoiceBox<String>) scene.lookup("#choose_architecture");
        choiceBoxArchitecture.setItems(FXCollections.observableArrayList(
                "One thread per client", "With SingleThreadExecutor", "Non-blocking")
        );
        choiceBoxArchitecture.getSelectionModel().selectFirst();

        ChoiceBox<String> choiceBoxParameter = (ChoiceBox<String>) scene.lookup("#parameter");
        choiceBoxParameter.setItems(FXCollections.observableArrayList(
                "N (array size)", "M (number of clients)", "∆ (interval between queries)")
        );
        choiceBoxParameter.getSelectionModel().selectFirst();

        primaryStage.setScene(scene);
    }
}
