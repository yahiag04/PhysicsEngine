package it.unibs.pajc.ui.components;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.Vector2D;
import it.unibs.pajc.ui.rendering.GridRenderer;
import it.unibs.pajc.ui.rendering.ParticleRenderer;
import it.unibs.pajc.ui.rendering.TrajectoryRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class ParticleCanvas extends Canvas {

    private final GraphicsContext gc;
    private final GridRenderer gridRenderer;
    private final ParticleRenderer particleRenderer;
    private final TrajectoryRenderer trajectoryRenderer;

    private final List<Particle> particles;
    private boolean showGrid = true;
    private boolean showTrajectory = true;

    // Camera system
    private boolean cameraFollowEnabled = false;
    private Particle cameraTarget = null;
    private double cameraX = 0;
    private double cameraY = 0;
    private double cameraLerpFactor = 0.1; // Smoothing factor (0-1, lower = smoother)
    private double zoom = 1.0;

    public ParticleCanvas(double width, double height) {
        super(width, height);
        this.gc = getGraphicsContext2D();
        this.gridRenderer = new GridRenderer();
        this.particleRenderer = new ParticleRenderer();
        this.trajectoryRenderer = new TrajectoryRenderer();
        this.particles = new ArrayList<>();
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

    public void render() {
        updateCamera();

        gc.save();

        gc.setFill(Color.rgb(26, 26, 46));
        gc.fillRect(0, 0, getWidth(), getHeight());

        // Apply camera transform
        gc.translate(getWidth() / 2, getHeight() / 2);
        gc.scale(zoom, zoom);
        gc.translate(-cameraX, -cameraY);

        if (showGrid) {
            renderGrid();
        }

        for (Particle particle : particles) {
            if (showTrajectory && particle.getMotionCalculator() != null) {
                trajectoryRenderer.renderWithCamera(gc, particle, particle.getMotionCalculator(),
                                          cameraX, cameraY, getWidth() / zoom, getHeight() / zoom);
            }
        }

        for (Particle particle : particles) {
            particleRenderer.renderWithCamera(gc, particle);
        }

        gc.restore();

        // Draw camera info overlay if following
        if (cameraFollowEnabled && cameraTarget != null) {
            drawCameraInfo();
        }
    }

    private void updateCamera() {
        if (cameraFollowEnabled && cameraTarget != null) {
            Vector2D targetPos = cameraTarget.getPosition();

            // Smooth interpolation to target
            cameraX = cameraX + (targetPos.x() - cameraX) * cameraLerpFactor;
            cameraY = cameraY + (targetPos.y() - cameraY) * cameraLerpFactor;
        }
    }

    private void renderGrid() {
        double gridSize = 50;
        gc.setStroke(Color.rgb(50, 50, 70, 0.5));
        gc.setLineWidth(0.5 / zoom);

        double viewWidth = getWidth() / zoom;
        double viewHeight = getHeight() / zoom;
        double startX = cameraX - viewWidth;
        double endX = cameraX + viewWidth;
        double startY = cameraY - viewHeight;
        double endY = cameraY + viewHeight;

        startX = Math.floor(startX / gridSize) * gridSize;
        startY = Math.floor(startY / gridSize) * gridSize;

        for (double x = startX; x <= endX; x += gridSize) {
            gc.strokeLine(x, startY, x, endY);
        }

        for (double y = startY; y <= endY; y += gridSize) {
            gc.strokeLine(startX, y, endX, y);
        }

        // Draw origin axes
        gc.setStroke(Color.rgb(100, 100, 120, 0.8));
        gc.setLineWidth(1 / zoom);
        gc.strokeLine(startX, 0, endX, 0);
        gc.strokeLine(0, startY, 0, endY);
    }

    private void drawCameraInfo() {
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRoundRect(10, getHeight() - 60, 200, 50, 10, 10);

        gc.setFill(Color.LIME);
        gc.fillText(String.format("Camera: (%.0f, %.0f)", cameraX, cameraY), 20, getHeight() - 40);
        gc.fillText(String.format("Zoom: %.1fx | Following: ON", zoom), 20, getHeight() - 20);
    }

    // Camera control methods
    public void setCameraFollow(boolean enabled) {
        this.cameraFollowEnabled = enabled;
        if (!enabled) {
            // Reset camera to center when disabling
            cameraX = getWidth() / 2;
            cameraY = getHeight() / 2;
        }
    }

    public void setCameraTarget(Particle target) {
        this.cameraTarget = target;
        if (target != null && cameraFollowEnabled) {
            // Snap to target initially
            cameraX = target.getPosition().x();
            cameraY = target.getPosition().y();
        }
    }

    public void setCameraLerpFactor(double factor) {
        this.cameraLerpFactor = Math.max(0.01, Math.min(1.0, factor));
    }

    public void setZoom(double zoom) {
        this.zoom = Math.max(0.1, Math.min(3.0, zoom));
    }

    public void resetCamera() {
        cameraX = getWidth() / 2;
        cameraY = getHeight() / 2;
        zoom = 1.0;
    }

    public boolean isCameraFollowEnabled() {
        return cameraFollowEnabled;
    }

    public void setParticles(List<Particle> particles) {
        this.particles.clear();
        this.particles.addAll(particles);
    }

    public void addParticle(Particle particle) {
        this.particles.add(particle);
    }

    public void removeParticle(Particle particle) {
        this.particles.remove(particle);
    }

    public void clear() {
        this.particles.clear();
        cameraTarget = null;
        gc.setFill(Color.rgb(26, 26, 46));
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public void setShowTrajectory(boolean showTrajectory) {
        this.showTrajectory = showTrajectory;
    }

    public ParticleRenderer getParticleRenderer() {
        return particleRenderer;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public double getCameraX() {
        return cameraX;
    }

    public double getCameraY() {
        return cameraY;
    }
}
