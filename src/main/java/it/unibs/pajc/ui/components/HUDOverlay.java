package it.unibs.pajc.ui.components;

import it.unibs.pajc.collision.CollisionEvent;
import it.unibs.pajc.core.Particle;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Map;

public class HUDOverlay extends VBox {

    private final Label positionLabel;
    private final Label velocityLabel;
    private final Label accelerationLabel;
    private final Label timeLabel;
    private final Label kineticEnergyLabel;
    private final Label momentumLabel;
    private final VBox motionParamsBox;
    private final VBox playersBox;
    private final VBox collisionLogBox;
    private final ScrollPane collisionScrollPane;

    private boolean multiplayerMode = false;
    private int myPlayerId = -1;

    public HUDOverlay() {
        setSpacing(5);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");
        setMaxWidth(300);
        setPickOnBounds(false);

        Label titleLabel = createLabel("HUD", 16, true);
        positionLabel = createLabel("Position: (0, 0)", 12, false);
        velocityLabel = createLabel("Velocity: (0, 0)", 12, false);
        accelerationLabel = createLabel("Acceleration: (0, 0)", 12, false);
        timeLabel = createLabel("Time: 0.00s", 12, false);
        kineticEnergyLabel = createLabel("KE: 0 J", 12, false);
        momentumLabel = createLabel("Momentum: (0, 0)", 12, false);

        motionParamsBox = new VBox(3);
        motionParamsBox.setPadding(new Insets(5, 0, 0, 0));

        playersBox = new VBox(5);
        playersBox.setPadding(new Insets(5, 0, 0, 0));
        playersBox.setVisible(false);
        playersBox.setManaged(false);

        collisionLogBox = new VBox(3);
        collisionScrollPane = new ScrollPane(collisionLogBox);
        collisionScrollPane.setMaxHeight(100);
        collisionScrollPane.setFitToWidth(true);
        collisionScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        collisionScrollPane.setVisible(false);
        collisionScrollPane.setManaged(false);

        getChildren().addAll(
            titleLabel,
            positionLabel,
            velocityLabel,
            accelerationLabel,
            timeLabel,
            kineticEnergyLabel,
            momentumLabel,
            motionParamsBox,
            playersBox,
            collisionScrollPane
        );
    }

    private Label createLabel(String text, int fontSize, boolean bold) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospace", bold ? FontWeight.BOLD : FontWeight.NORMAL, fontSize));
        label.setStyle("-fx-text-fill: #00ff00;");
        return label;
    }

    public void updateSinglePlayer(Particle particle, double time) {
        if (particle == null) return;

        positionLabel.setText(String.format("Position: (%.1f, %.1f)",
            particle.getPosition().x(), particle.getPosition().y()));
        velocityLabel.setText(String.format("Velocity: (%.2f, %.2f) m/s",
            particle.getVelocity().x(), particle.getVelocity().y()));
        accelerationLabel.setText(String.format("Accel: (%.2f, %.2f) m/s^2",
            particle.getAcceleration().x(), particle.getAcceleration().y()));
        timeLabel.setText(String.format("Time: %.2fs", time));
        kineticEnergyLabel.setText(String.format("KE: %.2f J", particle.kineticEnergy()));
        momentumLabel.setText(String.format("Momentum: (%.2f, %.2f)",
            particle.momentum().x(), particle.momentum().y()));

        if (particle.getMotionCalculator() != null) {
            updateMotionParams(particle.getMotionCalculator().getParameters());
        }
    }

    public void updateMotionParams(Map<String, Double> params) {
        motionParamsBox.getChildren().clear();

        if (params != null && !params.isEmpty()) {
            Label header = createLabel("Motion Parameters:", 11, true);
            motionParamsBox.getChildren().add(header);

            for (Map.Entry<String, Double> entry : params.entrySet()) {
                Label paramLabel = createLabel(
                    String.format("  %s: %.3f", entry.getKey(), entry.getValue()),
                    10, false
                );
                motionParamsBox.getChildren().add(paramLabel);
            }
        }
    }

    public void setMultiplayerMode(boolean multiplayer) {
        this.multiplayerMode = multiplayer;
        collisionScrollPane.setVisible(multiplayer);
        collisionScrollPane.setManaged(multiplayer);
        playersBox.setVisible(multiplayer);
        playersBox.setManaged(multiplayer);

        if (multiplayer) {
            positionLabel.setVisible(false);
            positionLabel.setManaged(false);
            velocityLabel.setVisible(false);
            velocityLabel.setManaged(false);
            accelerationLabel.setVisible(false);
            accelerationLabel.setManaged(false);
            kineticEnergyLabel.setVisible(false);
            kineticEnergyLabel.setManaged(false);
            momentumLabel.setVisible(false);
            momentumLabel.setManaged(false);
            motionParamsBox.setVisible(false);
            motionParamsBox.setManaged(false);
        } else {
            positionLabel.setVisible(true);
            positionLabel.setManaged(true);
            velocityLabel.setVisible(true);
            velocityLabel.setManaged(true);
            accelerationLabel.setVisible(true);
            accelerationLabel.setManaged(true);
            kineticEnergyLabel.setVisible(true);
            kineticEnergyLabel.setManaged(true);
            momentumLabel.setVisible(true);
            momentumLabel.setManaged(true);
            motionParamsBox.setVisible(true);
            motionParamsBox.setManaged(true);
        }
    }

    public void setMyPlayerId(int playerId) {
        this.myPlayerId = playerId;
    }

    public void updateMultiPlayer(List<Particle> particles, List<CollisionEvent> collisions) {
        if (particles.isEmpty()) return;

        timeLabel.setText(String.format("Players Online: %d", particles.size()));

        // Update all players info
        playersBox.getChildren().clear();

        for (Particle p : particles) {
            String playerLabel = (p.getPlayerId() == myPlayerId) ? "YOU" : "P" + p.getPlayerId();
            String colorHex = String.format("#%02x%02x%02x",
                (int)(p.getColor().getRed() * 255),
                (int)(p.getColor().getGreen() * 255),
                (int)(p.getColor().getBlue() * 255));

            Label header = new Label(String.format("--- %s ---", playerLabel));
            header.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            header.setStyle("-fx-text-fill: " + colorHex + ";");

            Label posLabel = new Label(String.format("  Pos: (%.0f, %.0f)",
                p.getPosition().x(), p.getPosition().y()));
            posLabel.setFont(Font.font("Monospace", 10));
            posLabel.setStyle("-fx-text-fill: #cccccc;");

            Label velLabel = new Label(String.format("  Vel: (%.1f, %.1f) | %.1f m/s",
                p.getVelocity().x(), p.getVelocity().y(), p.getVelocity().magnitude()));
            velLabel.setFont(Font.font("Monospace", 10));
            velLabel.setStyle("-fx-text-fill: #cccccc;");

            playersBox.getChildren().addAll(header, posLabel, velLabel);
        }

        if (collisions != null) {
            for (CollisionEvent event : collisions) {
                addCollisionEvent(event);
            }
        }
    }

    public void addCollisionEvent(CollisionEvent event) {
        if (!multiplayerMode) return;

        Label eventLabel = new Label(String.format(
            "Collision: P%d vs P%d @ %.1f m/s",
            event.particleIdA().hashCode() % 100,
            event.particleIdB().hashCode() % 100,
            event.impactSpeed()
        ));
        eventLabel.setFont(Font.font("Monospace", 9));
        eventLabel.setStyle("-fx-text-fill: #ffaa00;");

        collisionLogBox.getChildren().add(0, eventLabel);

        if (collisionLogBox.getChildren().size() > 20) {
            collisionLogBox.getChildren().remove(collisionLogBox.getChildren().size() - 1);
        }
    }

    public void clearCollisions() {
        collisionLogBox.getChildren().clear();
    }
}
