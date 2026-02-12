package it.unibs.pajc.ui.scenes;

import it.unibs.pajc.core.PhysicsConstants;
import it.unibs.pajc.network.client.GameClient;
import it.unibs.pajc.network.server.GameServer;
import it.unibs.pajc.ui.PhysicsApp;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MultiPlayerLobbyScene {

    private final PhysicsApp app;
    private final Scene scene;

    private boolean isHost;
    private GameServer server;
    private GameClient client;

    private Label titleLabel;
    private TextField hostField;
    private TextField portField;
    private TextField nameField;
    private ColorPicker colorPicker;
    private TextArea logArea;
    private Button actionButton;
    private Button startGameButton;
    private Button backButton;

    public MultiPlayerLobbyScene(PhysicsApp app) {
        this.app = app;
        this.scene = createScene();
    }

    private Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");

        titleLabel = new Label("Multiplayer");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: #e94560;");

        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(400);

        hostField = createTextField("localhost");
        portField = createTextField(String.valueOf(PhysicsConstants.SERVER_PORT));
        nameField = createTextField("Player");
        colorPicker = new ColorPicker(Color.DODGERBLUE);
        colorPicker.setPrefWidth(300);

        formBox.getChildren().addAll(
            createLabeledField("Host Address:", hostField),
            createLabeledField("Port:", portField),
            createLabeledField("Player Name:", nameField),
            createLabeledField("Color:", colorPicker)
        );

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        logArea.setMaxWidth(400);
        logArea.setStyle("-fx-control-inner-background: #0f0f1a; -fx-text-fill: #00ff00; " +
                        "-fx-font-family: monospace;");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        actionButton = createStyledButton("Connect", "#27ae60");
        startGameButton = createStyledButton("Start Game", "#e94560");
        startGameButton.setVisible(false);
        startGameButton.setManaged(false);

        backButton = createStyledButton("Back", "#3498db");
        backButton.setOnAction(e -> goBack());

        actionButton.setOnAction(e -> handleAction());
        startGameButton.setOnAction(e -> startGame());

        buttonBox.getChildren().addAll(actionButton, startGameButton, backButton);

        root.getChildren().addAll(titleLabel, formBox, logArea, buttonBox);

        return new Scene(root, 600, 600);
    }

    private HBox createLabeledField(String labelText, javafx.scene.Node field) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: white;");
        label.setPrefWidth(120);

        if (field instanceof TextField tf) {
            tf.setPrefWidth(180);
        }

        box.getChildren().addAll(label, field);
        return box;
    }

    private TextField createTextField(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-border-color: #0f3460;");
        return field;
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(120);
        btn.setPrefHeight(40);
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 5; -fx-cursor: hand;", color
        ));
        return btn;
    }

    public void setup(boolean isHost) {
        this.isHost = isHost;

        if (isHost) {
            titleLabel.setText("Host Game");
            hostField.setDisable(true);
            hostField.setText("localhost");
            actionButton.setText("Start Server");
            startGameButton.setVisible(false);
            startGameButton.setManaged(false);
        } else {
            titleLabel.setText("Join Game");
            hostField.setDisable(false);
            actionButton.setText("Connect");
            startGameButton.setVisible(false);
            startGameButton.setManaged(false);
        }

        logArea.clear();
        log("Ready to " + (isHost ? "host" : "join") + " a game");
    }

    private void handleAction() {
        if (isHost) {
            if (server == null || !server.isRunning()) {
                startServer();
            }
        } else {
            if (client == null || !client.isConnected()) {
                connectToServer();
            }
        }
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());

            server = new GameServer();
            server.setStatusCallback(msg -> Platform.runLater(() -> log(msg)));
            server.start(port);

            log("Server started, waiting for players...");
            actionButton.setText("Server Running");
            actionButton.setDisable(true);

            startGameButton.setVisible(true);
            startGameButton.setManaged(true);

            connectAsHost(port);
        } catch (Exception e) {
            log("Failed to start server: " + e.getMessage());
        }
    }

    private void connectAsHost(int port) {
        client = new GameClient();
        client.setStatusHandler(msg -> Platform.runLater(() -> log(msg)));

        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Host";

        if (client.connect("localhost", port, name, colorPicker.getValue())) {
            setupClientHandlers();
            client.startListening();
            log("Connected to own server as " + name);
        }
    }

    private void connectToServer() {
        String host = hostField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Player";

        client = new GameClient();
        client.setStatusHandler(msg -> Platform.runLater(() -> log(msg)));

        if (client.connect(host, port, name, colorPicker.getValue())) {
            setupClientHandlers();
            client.startListening();

            actionButton.setText("Connected");
            actionButton.setDisable(true);
            log("Waiting for host to start game...");
        } else {
            log("Failed to connect");
        }
    }

    private void setupClientHandlers() {
        client.setGameStartHandler(msg -> Platform.runLater(() -> {
            log("Game starting!");
            app.getMultiPlayerGameScene().initializeGame(client, msg);
            app.showMultiPlayerGame();
        }));

        client.setDisconnectHandler(() -> Platform.runLater(() -> {
            log("Disconnected from server");
            actionButton.setDisable(false);
            actionButton.setText(isHost ? "Start Server" : "Connect");
        }));
    }

    private void startGame() {
        if (server != null && server.isRunning()) {
            if (server.getPlayerCount() < 1) {
                log("Need at least 1 player to start");
                return;
            }
            server.startGame();
        }
    }

    private void goBack() {
        cleanup();
        app.showMainMenu();
    }

    public void cleanup() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        if (server != null) {
            server.stop();
            server = null;
        }

        actionButton.setDisable(false);
        startGameButton.setVisible(false);
        startGameButton.setManaged(false);
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    public Scene getScene() {
        return scene;
    }

    public GameClient getClient() {
        return client;
    }

    public GameServer getServer() {
        return server;
    }
}
