package it.unibs.pajc.ui.rendering;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.Vector2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

public class ParticleRenderer {

    private boolean showVelocityVector = true;
    private boolean showAccelerationVector = false;

    public void render(GraphicsContext gc, Particle particle, double canvasWidth, double canvasHeight) {
        renderWithCamera(gc, particle);
    }

    public void renderWithCamera(GraphicsContext gc, Particle particle) {
        Vector2D pos = particle.getPosition();
        double radius = particle.getRadius();
        Color color = particle.getColor();

        double screenX = pos.x();
        double screenY = pos.y();

        RadialGradient gradient = new RadialGradient(
            0, 0, 0.3, 0.3, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, color.brighter()),
            new Stop(0.7, color),
            new Stop(1, color.darker())
        );

        gc.setFill(gradient);
        gc.fillOval(screenX - radius, screenY - radius, radius * 2, radius * 2);

        gc.setStroke(color.darker().darker());
        gc.setLineWidth(2);
        gc.strokeOval(screenX - radius, screenY - radius, radius * 2, radius * 2);

        if (showVelocityVector) {
            drawVector(gc, pos, particle.getVelocity().multiply(0.1), Color.LIME, 2);
        }

        if (showAccelerationVector) {
            drawVector(gc, pos, particle.getAcceleration().multiply(0.5), Color.ORANGE, 2);
        }
    }

    private void drawVector(GraphicsContext gc, Vector2D origin, Vector2D vector, Color color, double lineWidth) {
        if (vector.magnitudeSquared() < 1) return;

        gc.setStroke(color);
        gc.setLineWidth(lineWidth);

        double endX = origin.x() + vector.x();
        double endY = origin.y() + vector.y();

        gc.strokeLine(origin.x(), origin.y(), endX, endY);

        double arrowSize = 8;
        double angle = vector.angle();
        double arrowAngle = Math.toRadians(150);

        double arrow1X = endX + arrowSize * Math.cos(angle + arrowAngle);
        double arrow1Y = endY + arrowSize * Math.sin(angle + arrowAngle);
        double arrow2X = endX + arrowSize * Math.cos(angle - arrowAngle);
        double arrow2Y = endY + arrowSize * Math.sin(angle - arrowAngle);

        gc.strokeLine(endX, endY, arrow1X, arrow1Y);
        gc.strokeLine(endX, endY, arrow2X, arrow2Y);
    }

    public void setShowVelocityVector(boolean show) {
        this.showVelocityVector = show;
    }

    public void setShowAccelerationVector(boolean show) {
        this.showAccelerationVector = show;
    }
}
