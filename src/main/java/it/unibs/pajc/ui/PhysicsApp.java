package it.unibs.pajc.ui;

import it.unibs.pajc.ui.scenes.MainMenuScene;
import it.unibs.pajc.ui.scenes.MultiPlayerGameScene;
import it.unibs.pajc.ui.scenes.MultiPlayerLobbyScene;
import it.unibs.pajc.ui.scenes.SinglePlayerScene;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PhysicsApp extends Application {

    private static PhysicsApp instance;

    private Stage primaryStage;
    private MainMenuScene mainMenuScene;
    private SinglePlayerScene singlePlayerScene;
    private MultiPlayerLobbyScene lobbyScene;
    private MultiPlayerGameScene multiPlayerGameScene;

    public static PhysicsApp getInstance() {
        return instance;
    }

    @Override
    public void start(Stage stage) {
        instance = this;
        this.primaryStage = stage;

        primaryStage.setTitle("Physics Engine");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        mainMenuScene = new MainMenuScene(this);
        singlePlayerScene = new SinglePlayerScene(this);
        lobbyScene = new MultiPlayerLobbyScene(this);
        multiPlayerGameScene = new MultiPlayerGameScene(this);

        showMainMenu();

        primaryStage.setOnCloseRequest(e -> {
            cleanup();
            Platform.exit();
        });

        primaryStage.show();
    }

    public void showMainMenu() {
        singlePlayerScene.stop();
        multiPlayerGameScene.stop();
        primaryStage.setScene(mainMenuScene.getScene());
    }

    public void showSinglePlayer() {
        singlePlayerScene.reset();
        primaryStage.setScene(singlePlayerScene.getScene());
    }

    public void showLobby(boolean isHost) {
        lobbyScene.setup(isHost);
        primaryStage.setScene(lobbyScene.getScene());
    }

    public void showMultiPlayerGame() {
        primaryStage.setScene(multiPlayerGameScene.getScene());
        multiPlayerGameScene.start();
    }

    public MultiPlayerLobbyScene getLobbyScene() {
        return lobbyScene;
    }

    public MultiPlayerGameScene getMultiPlayerGameScene() {
        return multiPlayerGameScene;
    }

    private void cleanup() {
        singlePlayerScene.stop();
        multiPlayerGameScene.stop();
        lobbyScene.cleanup();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
