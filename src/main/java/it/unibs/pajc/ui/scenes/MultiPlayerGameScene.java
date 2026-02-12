package it.unibs.pajc.ui.scenes;

import it.unibs.pajc.collision.CollisionEvent;
import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.PhysicsConstants;
import it.unibs.pajc.core.Vector2D;
import it.unibs.pajc.network.client.GameClient;
import it.unibs.pajc.network.protocol.GameStartMessage;
import it.unibs.pajc.network.protocol.StateUpdateMessage;
import it.unibs.pajc.ui.PhysicsApp;
import it.unibs.pajc.ui.components.HUDOverlay;
import it.unibs.pajc.ui.components.ParticleCanvas;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MultiPlayerGameScene {

    private final PhysicsApp app;
    private final Scene scene;
    private final ParticleCanvas canvas;
    private final HUDOverlay hud;

    private GameClient client;
    private final Map<String, Particle> particles;
    private final Set<KeyCode> pressedKeys;

    private AnimationTimer gameLoop;
    private AnimationTimer inputLoop;
    private boolean running;

    private Label connectionStatus;
    private Label playerIdLabel;

    public MultiPlayerGameScene(PhysicsApp app) {
        this.app = app;
        this.particles = new ConcurrentHashMap<>();
        this.pressedKeys = new HashSet<>();
        this.canvas = new ParticleCanvas(1000, 700);
        this.hud = new HUDOverlay();
        this.hud.setMultiplayerMode(true);
        this.scene = createScene();
        setupGameLoop();
        setupInputLoop();
    }

    private Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f0f1a;");

        StackPane canvasArea = new StackPane();
        canvasArea.setStyle("-fx-background-color: #1a1a2e;");

        canvas.widthProperty().bind(canvasArea.widthProperty());
        canvas.heightProperty().bind(canvasArea.heightProperty());

        StackPane.setAlignment(hud, Pos.TOP_LEFT);
        StackPane.setMargin(hud, new Insets(10));

        VBox infoBox = createInfoBox();
        StackPane.setAlignment(infoBox, Pos.TOP_RIGHT);
        StackPane.setMargin(infoBox, new Insets(10));

        canvasArea.getChildren().addAll(canvas, hud, infoBox);
        root.setCenter(canvasArea);

        HBox bottomBar = createBottomBar();
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 1200, 800);
        setupKeyHandlers(scene);

        return scene;
    }

    private VBox createInfoBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");
        box.setMaxWidth(200);

        Label controlsTitle = new Label("Controls");
        controlsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        controlsTitle.setStyle("-fx-text-fill: #e94560;");

        Label wasd = new Label("WASD or Arrow Keys\nto move your particle");
        wasd.setStyle("-fx-text-fill: white; -fx-font-size: 11;");

        playerIdLabel = new Label("Player ID: -");
        playerIdLabel.setStyle("-fx-text-fill: #00ff00;");

        connectionStatus = new Label("Status: Connecting...");
        connectionStatus.setStyle("-fx-text-fill: #ffaa00;");

        box.getChildren().addAll(controlsTitle, wasd, playerIdLabel, connectionStatus);
        return box;
    }

    private HBox createBottomBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #16213e;");

        Button disconnectBtn = new Button("Disconnect");
        disconnectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-background-radius: 5;");
        disconnectBtn.setOnAction(e -> disconnect());

        Label instructions = new Label("Use WASD or Arrow Keys to move | Collide with other players!");
        instructions.setStyle("-fx-text-fill: #888888;");

        bar.getChildren().addAll(disconnectBtn, instructions);
        return bar;
    }

    private void setupKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(e -> {
            pressedKeys.add(e.getCode());
        });

        scene.setOnKeyReleased(e -> {
            pressedKeys.remove(e.getCode());
        });
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (running) {
                    canvas.setParticles(List.copyOf(particles.values()));
                    canvas.render();
                    hud.updateMultiPlayer(List.copyOf(particles.values()), null);
                }
            }
        };
    }

    private void setupInputLoop() {
        inputLoop = new AnimationTimer() {
            private long lastSend = 0;
            private static final long INPUT_INTERVAL = 16_666_666L; // ~60 Hz
            private Vector2D lastDirection = Vector2D.ZERO;

            @Override
            public void handle(long now) {
                if (!running || client == null || !client.isConnected()) return;

                if (now - lastSend >= INPUT_INTERVAL) {
                    Vector2D direction = getInputDirection();
                    // Send input if direction changed or if actively pressing keys
                    if (direction.magnitudeSquared() > 0 || !direction.equals(lastDirection)) {
                        client.sendInput(direction);
                        lastDirection = direction;
                    }
                    lastSend = now;
                }
            }
        };
    }

    private Vector2D getInputDirection() {
        double dx = 0, dy = 0;

        if (pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP)) dy -= 1;
        if (pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN)) dy += 1;
        if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT)) dx -= 1;
        if (pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT)) dx += 1;

        Vector2D dir = new Vector2D(dx, dy);
        return dir.magnitudeSquared() > 0 ? dir.normalize() : Vector2D.ZERO;
    }

    public void initializeGame(GameClient client, GameStartMessage startMsg) {
        this.client = client;
        this.particles.clear();

        for (StateUpdateMessage.PlayerState state : startMsg.getInitialStates()) {
            Particle p = createParticleFromState(state);
            particles.put(state.particleId, p);
        }

        client.setStateUpdateHandler(this::handleStateUpdate);
        client.setPlayerLeftHandler(msg -> Platform.runLater(() -> {
            particles.values().removeIf(p -> p.getPlayerId() == msg.getLeftPlayerId());
        }));

        Platform.runLater(() -> {
            playerIdLabel.setText("Player ID: " + client.getPlayerId());
            connectionStatus.setText("Status: Connected");
            connectionStatus.setStyle("-fx-text-fill: #00ff00;");
            hud.setMyPlayerId(client.getPlayerId());
        });
    }

    private void handleStateUpdate(StateUpdateMessage update) {
        Platform.runLater(() -> {
            for (StateUpdateMessage.PlayerState state : update.getPlayerStates()) {
                Particle existing = particles.get(state.particleId);
                if (existing != null) {
                    existing.setPosition(state.getPosition());
                    existing.setVelocity(state.getVelocity());
                } else {
                    Particle p = createParticleFromState(state);
                    particles.put(state.particleId, p);
                }
            }

            List<CollisionEvent> collisions = update.getRecentCollisions();
            if (collisions != null && !collisions.isEmpty()) {
                for (CollisionEvent event : collisions) {
                    hud.addCollisionEvent(event);
                }
            }
        });
    }

    private Particle createParticleFromState(StateUpdateMessage.PlayerState state) {
        return new Particle(
            state.particleId,
            state.playerId,
            PhysicsConstants.DEFAULT_PARTICLE_MASS,
            state.radius,
            Color.color(state.red, state.green, state.blue),
            state.getPosition(),
            state.getVelocity()
        );
    }

    public void start() {
        running = true;
        gameLoop.start();
        inputLoop.start();
        pressedKeys.clear();
    }

    public void stop() {
        running = false;
        gameLoop.stop();
        inputLoop.stop();
        pressedKeys.clear();
    }

    private void disconnect() {
        stop();
        if (client != null) {
            client.disconnect();
            client = null;
        }
        particles.clear();
        app.getLobbyScene().cleanup();
        app.showMainMenu();
    }

    public Scene getScene() {
        return scene;
    }
}
