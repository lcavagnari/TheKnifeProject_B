package com.example.demo3;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public final class Navigation {

    private static final Deque<Runnable> backstack = new ArrayDeque<>();

    private Navigation() {}

    public static void pushBack(Runnable goBack) {
        backstack.push(goBack);
    }

    public static void goBack() {
        if (!backstack.isEmpty()) {
            backstack.pop().run();
        }
    }

    public static void clearBackstack() {
        backstack.clear();
    }

    public static void navigateTo(Stage stage, String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}