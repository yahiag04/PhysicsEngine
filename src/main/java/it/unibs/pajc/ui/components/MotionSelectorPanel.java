package it.unibs.pajc.ui.components;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.Vector2D;
import it.unibs.pajc.core.motion.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class MotionSelectorPanel extends VBox {

    private final ComboBox<MotionType> motionTypeCombo;
    private final VBox paramsBox;

    private TextField gravityField;
    private TextField centerXField;
    private TextField centerYField;
    private TextField radiusField;
    private TextField angularVelField;
    private TextField amplitudeField;
    private TextField frequencyField;
    private TextField lengthField;
    private TextField maxAngleField;

    public MotionSelectorPanel() {
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10;");

        Label titleLabel = new Label("Motion Type");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        motionTypeCombo = new ComboBox<>();
        motionTypeCombo.getItems().addAll(
            MotionType.PROJECTILE,
            MotionType.CIRCULAR,
            MotionType.SIMPLE_HARMONIC,
            MotionType.PENDULUM,
            MotionType.LINEAR,
            MotionType.FREEFALL
        );
        motionTypeCombo.setValue(MotionType.PROJECTILE);
        motionTypeCombo.setPrefWidth(200);
        motionTypeCombo.setStyle("-fx-background-color: #16213e;");

        paramsBox = new VBox(10);
        paramsBox.setPadding(new Insets(10, 0, 0, 0));

        motionTypeCombo.setOnAction(e -> updateParamsUI());
        updateParamsUI();

        getChildren().addAll(titleLabel, motionTypeCombo, paramsBox);
    }

    private void updateParamsUI() {
        paramsBox.getChildren().clear();
        MotionType type = motionTypeCombo.getValue();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        int row = 0;

        switch (type) {
            case PROJECTILE, FREEFALL -> {
                gravityField = createTextField("9.81");
                grid.add(createLabel("Gravity (m/s^2):"), 0, row);
                grid.add(gravityField, 1, row++);
            }
            case CIRCULAR -> {
                centerXField = createTextField("400");
                centerYField = createTextField("300");
                radiusField = createTextField("100");
                angularVelField = createTextField("2");

                grid.add(createLabel("Center X:"), 0, row);
                grid.add(centerXField, 1, row++);
                grid.add(createLabel("Center Y:"), 0, row);
                grid.add(centerYField, 1, row++);
                grid.add(createLabel("Radius:"), 0, row);
                grid.add(radiusField, 1, row++);
                grid.add(createLabel("Angular Vel (rad/s):"), 0, row);
                grid.add(angularVelField, 1, row++);
            }
            case SIMPLE_HARMONIC -> {
                amplitudeField = createTextField("100");
                frequencyField = createTextField("1");

                grid.add(createLabel("Amplitude (px):"), 0, row);
                grid.add(amplitudeField, 1, row++);
                grid.add(createLabel("Angular Freq (rad/s):"), 0, row);
                grid.add(frequencyField, 1, row++);
            }
            case PENDULUM -> {
                lengthField = createTextField("150");
                maxAngleField = createTextField("30");
                gravityField = createTextField("9.81");

                grid.add(createLabel("Length (px):"), 0, row);
                grid.add(lengthField, 1, row++);
                grid.add(createLabel("Max Angle (deg):"), 0, row);
                grid.add(maxAngleField, 1, row++);
                grid.add(createLabel("Gravity (m/s^2):"), 0, row);
                grid.add(gravityField, 1, row++);
            }
            case LINEAR -> {
                // Linear motion uses initial velocity from particle config
                Label infoLabel = createLabel("Uses particle velocity");
                infoLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
                grid.add(infoLabel, 0, row++, 2, 1);
            }
        }

        paramsBox.getChildren().add(grid);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white;");
        return label;
    }

    private TextField createTextField(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setPrefWidth(120);
        field.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-border-color: #0f3460;");
        return field;
    }

    public MotionType getSelectedMotionType() {
        return motionTypeCombo.getValue();
    }

    public MotionCalculator createMotionCalculator(Particle particle) {
        MotionType type = motionTypeCombo.getValue();
        Vector2D pos = particle.getPosition();
        Vector2D vel = particle.getVelocity();

        return switch (type) {
            case PROJECTILE -> new ProjectileMotion(pos, vel, parseDouble(gravityField, 9.81));
            case CIRCULAR -> new CircularMotion(
                new Vector2D(parseDouble(centerXField, 400), parseDouble(centerYField, 300)),
                parseDouble(radiusField, 100),
                parseDouble(angularVelField, 2)
            );
            case SIMPLE_HARMONIC -> new SimpleHarmonicMotion(
                pos,
                parseDouble(amplitudeField, 100),
                parseDouble(frequencyField, 1)
            );
            case PENDULUM -> new PendulumMotion(
                new Vector2D(pos.x(), pos.y() - parseDouble(lengthField, 150)),
                parseDouble(lengthField, 150),
                parseDouble(maxAngleField, 30),
                parseDouble(gravityField, 9.81)
            );
            case LINEAR -> new LinearMotion(pos, vel);
            case FREEFALL -> new FreefallMotion(pos, parseDouble(gravityField, 9.81));
            default -> null;
        };
    }

    private double parseDouble(TextField field, double defaultValue) {
        if (field == null) return defaultValue;
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
