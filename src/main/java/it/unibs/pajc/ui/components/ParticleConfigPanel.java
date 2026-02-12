package it.unibs.pajc.ui.components;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.PhysicsConstants;
import it.unibs.pajc.core.Vector2D;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ParticleConfigPanel extends VBox {

    private final TextField massField;
    private final TextField radiusField;
    private final ColorPicker colorPicker;
    private final TextField posXField;
    private final TextField posYField;
    private final TextField velXField;
    private final TextField velYField;

    public ParticleConfigPanel() {
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10;");

        Label titleLabel = new Label("Particle Configuration");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        massField = createTextField("1.0");
        radiusField = createTextField("20");
        colorPicker = new ColorPicker(Color.DODGERBLUE);
        colorPicker.setPrefWidth(150);
        posXField = createTextField("400");
        posYField = createTextField("300");
        velXField = createTextField("50");
        velYField = createTextField("-80");

        int row = 0;
        grid.add(createLabel("Mass (kg):"), 0, row);
        grid.add(massField, 1, row++);

        grid.add(createLabel("Radius (px):"), 0, row);
        grid.add(radiusField, 1, row++);

        grid.add(createLabel("Color:"), 0, row);
        grid.add(colorPicker, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(createLabel("Position X:"), 0, row);
        grid.add(posXField, 1, row++);

        grid.add(createLabel("Position Y:"), 0, row);
        grid.add(posYField, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(createLabel("Velocity X:"), 0, row);
        grid.add(velXField, 1, row++);

        grid.add(createLabel("Velocity Y:"), 0, row);
        grid.add(velYField, 1, row++);

        getChildren().addAll(titleLabel, grid);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white;");
        return label;
    }

    private TextField createTextField(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setPrefWidth(150);
        field.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-border-color: #0f3460;");
        return field;
    }

    public Particle createParticle() {
        try {
            double mass = parseDouble(massField.getText(), PhysicsConstants.DEFAULT_PARTICLE_MASS);
            double radius = parseDouble(radiusField.getText(), PhysicsConstants.DEFAULT_PARTICLE_RADIUS);
            Color color = colorPicker.getValue();
            double posX = parseDouble(posXField.getText(), 400);
            double posY = parseDouble(posYField.getText(), 300);
            double velX = parseDouble(velXField.getText(), 0);
            double velY = parseDouble(velYField.getText(), 0);

            return new Particle(
                mass,
                radius,
                color,
                new Vector2D(posX, posY),
                new Vector2D(velX, velY)
            );
        } catch (NumberFormatException e) {
            showError("Invalid input. Please enter valid numbers.");
            return null;
        }
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public double getMass() {
        return parseDouble(massField.getText(), PhysicsConstants.DEFAULT_PARTICLE_MASS);
    }

    public double getRadius() {
        return parseDouble(radiusField.getText(), PhysicsConstants.DEFAULT_PARTICLE_RADIUS);
    }

    public Color getColor() {
        return colorPicker.getValue();
    }

    public Vector2D getPosition() {
        return new Vector2D(
            parseDouble(posXField.getText(), 400),
            parseDouble(posYField.getText(), 300)
        );
    }

    public Vector2D getVelocity() {
        return new Vector2D(
            parseDouble(velXField.getText(), 0),
            parseDouble(velYField.getText(), 0)
        );
    }

    public void setPosition(double x, double y) {
        posXField.setText(String.format("%.0f", x));
        posYField.setText(String.format("%.0f", y));
    }

    public void setVelocity(double vx, double vy) {
        velXField.setText(String.format("%.0f", vx));
        velYField.setText(String.format("%.0f", vy));
    }
}
