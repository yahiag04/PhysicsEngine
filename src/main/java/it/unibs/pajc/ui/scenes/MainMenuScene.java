package it.unibs.pajc.ui.scenes;

import it.unibs.pajc.ui.PhysicsApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainMenuScene {

    private final PhysicsApp app;
    private final Scene scene;

    public MainMenuScene(PhysicsApp app) {
        this.app = app;
        this.scene = createScene();
    }

    private Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");

        Label title = new Label("Physics Engine");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setStyle("-fx-text-fill: #e94560;");

        Label subtitle = new Label("2D Particle Simulation");
        subtitle.setFont(Font.font("Arial", 18));
        subtitle.setStyle("-fx-text-fill: #a0a0a0;");

        Button singlePlayerBtn = createMenuButton("Single Player");
        singlePlayerBtn.setOnAction(e -> app.showSinglePlayer());

        Button hostGameBtn = createMenuButton("Host Game");
        hostGameBtn.setOnAction(e -> app.showLobby(true));

        Button joinGameBtn = createMenuButton("Join Game");
        joinGameBtn.setOnAction(e -> app.showLobby(false));

        Button exitBtn = createMenuButton("Exit");
        exitBtn.setOnAction(e -> {
            app.getPrimaryStage().close();
        });

        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(singlePlayerBtn, hostGameBtn, joinGameBtn, exitBtn);

        root.getChildren().addAll(title, subtitle, buttonBox);

        return new Scene(root, 1000, 700);
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(250);
        btn.setPrefHeight(50);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setStyle(
            "-fx-background-color: #0f3460; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 25; " +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #e94560; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 25; " +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #0f3460; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 25; " +
            "-fx-cursor: hand;"
        ));
        return btn;
    }

    public Scene getScene() {
        return scene;
    }
}
