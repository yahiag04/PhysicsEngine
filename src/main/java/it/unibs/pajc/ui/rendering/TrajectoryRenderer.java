package it.unibs.pajc.ui.rendering;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.ParticleState;
import it.unibs.pajc.core.Vector2D;
import it.unibs.pajc.core.motion.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TrajectoryRenderer {

    private static final Color TRAJECTORY_COLOR = Color.rgb(255, 255, 255, 0.3);
    private static final double TRAJECTORY_LINE_WIDTH = 1.5;

    public void render(GraphicsContext gc, Particle particle, MotionCalculator motion,
                       double canvasWidth, double canvasHeight) {
        renderWithCamera(gc, particle, motion, canvasWidth / 2, canvasHeight / 2, canvasWidth, canvasHeight);
    }

    public void renderWithCamera(GraphicsContext gc, Particle particle, MotionCalculator motion,
                                 double cameraX, double cameraY, double viewWidth, double viewHeight) {
        if (motion == null) return;

        gc.setStroke(TRAJECTORY_COLOR);
        gc.setLineWidth(TRAJECTORY_LINE_WIDTH);

        switch (motion.getMotionType()) {
            case CIRCULAR -> renderCircularTrajectory(gc, (CircularMotion) motion);
            case SIMPLE_HARMONIC -> renderSHMTrajectory(gc, (SimpleHarmonicMotion) motion);
            case PENDULUM -> renderPendulumTrajectory(gc, (PendulumMotion) motion);
            case PROJECTILE -> renderProjectileTrajectory(gc, particle, (ProjectileMotion) motion, cameraY + viewHeight);
            case LINEAR -> renderLinearTrajectory(gc, particle, (LinearMotion) motion, viewWidth * 2, viewHeight * 2);
            case FREEFALL -> renderFreefallTrajectory(gc, particle, (FreefallMotion) motion, cameraY + viewHeight);
            default -> {}
        }
    }

    private void renderCircularTrajectory(GraphicsContext gc, CircularMotion motion) {
        Vector2D center = motion.getCenter();
        double radius = motion.getRadius();

        gc.strokeOval(
            center.x() - radius,
            center.y() - radius,
            radius * 2,
            radius * 2
        );

        gc.setFill(Color.rgb(255, 255, 255, 0.5));
        gc.fillOval(center.x() - 3, center.y() - 3, 6, 6);
    }

    private void renderSHMTrajectory(GraphicsContext gc, SimpleHarmonicMotion motion) {
        Vector2D equilibrium = motion.getEquilibriumPosition();
        double amplitude = motion.getAmplitude();

        gc.strokeLine(
            equilibrium.x() - amplitude,
            equilibrium.y(),
            equilibrium.x() + amplitude,
            equilibrium.y()
        );

        gc.setFill(Color.rgb(255, 255, 255, 0.5));
        gc.fillOval(equilibrium.x() - 3, equilibrium.y() - 3, 6, 6);
    }

    private void renderPendulumTrajectory(GraphicsContext gc, PendulumMotion motion) {
        Vector2D pivot = motion.getPivotPoint();
        double length = motion.getLength();

        gc.setStroke(Color.rgb(150, 150, 150, 0.5));
        gc.setLineDashes(5);
        gc.strokeLine(pivot.x(), pivot.y(), pivot.x(), pivot.y() + length);
        gc.setLineDashes(null);

        gc.setStroke(TRAJECTORY_COLOR);
        gc.strokeArc(
            pivot.x() - length,
            pivot.y() - length,
            length * 2,
            length * 2,
            240, 60,
            javafx.scene.shape.ArcType.OPEN
        );

        gc.setFill(Color.rgb(255, 100, 100, 0.8));
        gc.fillOval(pivot.x() - 5, pivot.y() - 5, 10, 10);
    }

    private void renderProjectileTrajectory(GraphicsContext gc, Particle particle,
                                            ProjectileMotion motion, double canvasHeight) {
        gc.beginPath();

        double dt = 0.05;
        boolean first = true;

        for (double t = 0; t < 20; t += dt) {
            ParticleState state = motion.calculate(particle, dt, t);
            Vector2D pos = state.position();

            if (pos.y() > canvasHeight + 100) break;

            if (first) {
                gc.moveTo(pos.x(), pos.y());
                first = false;
            } else {
                gc.lineTo(pos.x(), pos.y());
            }
        }

        gc.stroke();
    }

    private void renderLinearTrajectory(GraphicsContext gc, Particle particle,
                                        LinearMotion motion, double canvasWidth, double canvasHeight) {
        ParticleState start = motion.calculate(particle, 0, 0);
        ParticleState end = motion.calculate(particle, 0, 10);

        gc.setLineDashes(10, 5);
        gc.strokeLine(start.position().x(), start.position().y(),
                      end.position().x(), end.position().y());
        gc.setLineDashes(null);
    }

    private void renderFreefallTrajectory(GraphicsContext gc, Particle particle,
                                          FreefallMotion motion, double canvasHeight) {
        ParticleState start = motion.calculate(particle, 0, 0);

        gc.setLineDashes(10, 5);
        gc.strokeLine(start.position().x(), start.position().y(),
                      start.position().x(), canvasHeight);
        gc.setLineDashes(null);
    }
}
