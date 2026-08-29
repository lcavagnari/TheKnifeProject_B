package com.example.demo3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("GUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("The Knife Menu");
        stage.setScene(scene);
        stage.setMaximized(true);  // <-- This line opens the window maximized
        stage.show();
    }
}