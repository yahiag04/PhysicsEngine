package it.unibs.pajc.ui.scenes;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.motion.MotionCalculator;
import it.unibs.pajc.core.motion.MotionType;
import it.unibs.pajc.ui.PhysicsApp;
import it.unibs.pajc.ui.components.HUDOverlay;
import it.unibs.pajc.ui.components.MotionSelectorPanel;
import it.unibs.pajc.ui.components.ParticleCanvas;
import it.unibs.pajc.ui.components.ParticleConfigPanel;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class SinglePlayerScene {

    private final PhysicsApp app;
    private final Scene scene;
    private final ParticleCanvas canvas;
    private final HUDOverlay hud;
    private final ParticleConfigPanel configPanel;
    private final MotionSelectorPanel motionPanel;

    private Particle particle;
    private AnimationTimer gameLoop;
    private boolean running = false;
    private double elapsedTime = 0;
    private double timeScale = 1.0;

    private Button startButton;
    private Button resetButton;
    private CheckBox showGridCheck;
    private CheckBox showTrajectoryCheck;
    private CheckBox showVelocityCheck;
    private CheckBox cameraFollowCheck;
    private Slider zoomSlider;

    public SinglePlayerScene(PhysicsApp app) {
        this.app = app;
        this.canvas = new ParticleCanvas(800, 600);
        this.hud = new HUDOverlay();
        this.configPanel = new ParticleConfigPanel();
        this.motionPanel = new MotionSelectorPanel();
        this.scene = createScene();
        setupGameLoop();
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

        canvasArea.getChildren().addAll(canvas, hud);
        root.setCenter(canvasArea);

        VBox controlPanel = createControlPanel();
        controlPanel.setPrefWidth(300);
        root.setRight(controlPanel);

        HBox bottomBar = createBottomBar();
        root.setBottom(bottomBar);

        return new Scene(root, 1200, 750);
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #16213e;");

        startButton = createStyledButton("Start", "#27ae60");
        startButton.setOnAction(e -> toggleSimulation());

        resetButton = createStyledButton("Reset", "#e74c3c");
        resetButton.setOnAction(e -> reset());

        Button backButton = createStyledButton("Back to Menu", "#3498db");
        backButton.setOnAction(e -> {
            stop();
            app.showMainMenu();
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(startButton, resetButton);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #0f3460;");

        Label optionsLabel = new Label("Display Options");
        optionsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        showGridCheck = new CheckBox("Show Grid");
        showGridCheck.setSelected(true);
        showGridCheck.setStyle("-fx-text-fill: white;");
        showGridCheck.setOnAction(e -> canvas.setShowGrid(showGridCheck.isSelected()));

        showTrajectoryCheck = new CheckBox("Show Trajectory");
        showTrajectoryCheck.setSelected(true);
        showTrajectoryCheck.setStyle("-fx-text-fill: white;");
        showTrajectoryCheck.setOnAction(e -> canvas.setShowTrajectory(showTrajectoryCheck.isSelected()));

        showVelocityCheck = new CheckBox("Show Velocity Vector");
        showVelocityCheck.setSelected(true);
        showVelocityCheck.setStyle("-fx-text-fill: white;");
        showVelocityCheck.setOnAction(e -> canvas.getParticleRenderer().setShowVelocityVector(showVelocityCheck.isSelected()));

        cameraFollowCheck = new CheckBox("Camera Follow Particle");
        cameraFollowCheck.setSelected(false);
        cameraFollowCheck.setStyle("-fx-text-fill: white;");
        cameraFollowCheck.setOnAction(e -> {
            canvas.setCameraFollow(cameraFollowCheck.isSelected());
            if (cameraFollowCheck.isSelected() && particle != null) {
                canvas.setCameraTarget(particle);
            }
        });

        Label zoomLabel = new Label("Zoom:");
        zoomLabel.setStyle("-fx-text-fill: white;");

        zoomSlider = new Slider(0.3, 2.0, 1.0);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(0.5);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setZoom(newVal.doubleValue());
        });

        HBox zoomBox = new HBox(10);
        zoomBox.setAlignment(Pos.CENTER_LEFT);
        zoomBox.getChildren().addAll(zoomLabel, zoomSlider);

        VBox optionsBox = new VBox(8);
        optionsBox.getChildren().addAll(optionsLabel, showGridCheck, showTrajectoryCheck,
                                        showVelocityCheck, cameraFollowCheck, zoomBox);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox configContent = new VBox(15);
        configContent.getChildren().addAll(configPanel, motionPanel);
        scrollPane.setContent(configContent);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(
            buttonBox,
            backButton,
            sep1,
            optionsBox,
            new Separator(),
            scrollPane
        );

        return panel;
    }

    private HBox createBottomBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #16213e;");

        Label timeScaleLabel = new Label("Time Scale:");
        timeScaleLabel.setStyle("-fx-text-fill: white;");

        Slider timeScaleSlider = new Slider(0.1, 3.0, 1.0);
        timeScaleSlider.setPrefWidth(200);
        timeScaleSlider.setShowTickLabels(true);
        timeScaleSlider.setShowTickMarks(true);
        timeScaleSlider.setMajorTickUnit(0.5);

        Label scaleValueLabel = new Label("1.0x");
        scaleValueLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-family: monospace;");

        timeScaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            timeScale = newVal.doubleValue();
            scaleValueLabel.setText(String.format("%.1fx", timeScale));
        });

        Label statusLabel = new Label("Status: Stopped");
        statusLabel.setStyle("-fx-text-fill: #888888;");
        statusLabel.setId("statusLabel");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(timeScaleLabel, timeScaleSlider, scaleValueLabel, spacer, statusLabel);

        return bar;
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(120);
        btn.setPrefHeight(35);
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 5; -fx-cursor: hand;", color
        ));
        return btn;
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (running && particle != null) {
                    double scaledDelta = deltaTime * timeScale;
                    elapsedTime += scaledDelta;

                    particle.update(scaledDelta);

                    checkBoundaries();

                    canvas.render();
                    hud.updateSinglePlayer(particle, elapsedTime);
                }
            }
        };
    }

    private void checkBoundaries() {
        if (particle == null) return;

        double x = particle.getPosition().x();
        double y = particle.getPosition().y();
        double margin = 500;

        if (x < -margin || x > canvas.getWidth() + margin ||
            y < -margin || y > canvas.getHeight() + margin) {
            // Particle out of bounds - could pause or reset
        }
    }

    private void toggleSimulation() {
        if (running) {
            pauseSimulation();
        } else {
            startSimulation();
        }
    }

    private void startSimulation() {
        if (particle == null) {
            particle = configPanel.createParticle();
            if (particle == null) return;

            MotionCalculator motion = motionPanel.createMotionCalculator(particle);
            particle.setMotion(motionPanel.getSelectedMotionType(), motion);

            canvas.clear();
            canvas.addParticle(particle);

            // Set camera target if camera follow is enabled
            if (cameraFollowCheck.isSelected()) {
                canvas.setCameraTarget(particle);
            }
        }

        running = true;
        startButton.setText("Pause");
        startButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");

        updateStatus("Running");
    }

    private void pauseSimulation() {
        running = false;
        startButton.setText("Resume");
        startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");

        updateStatus("Paused");
    }

    public void reset() {
        running = false;
        elapsedTime = 0;
        particle = null;

        canvas.clear();
        canvas.resetCamera();
        zoomSlider.setValue(1.0);

        startButton.setText("Start");
        startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");

        updateStatus("Stopped");
        canvas.render();
    }

    private void updateStatus(String status) {
        scene.lookup("#statusLabel");
        Label statusLabel = (Label) scene.lookup("#statusLabel");
        if (statusLabel != null) {
            statusLabel.setText("Status: " + status);
            String color = switch (status) {
                case "Running" -> "#27ae60";
                case "Paused" -> "#f39c12";
                default -> "#888888";
            };
            statusLabel.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    public void stop() {
        running = false;
        gameLoop.stop();
    }

    public void start() {
        gameLoop.start();
    }

    public Scene getScene() {
        start();
        return scene;
    }
}
