package it.unibs.pajc.core.motion;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.ParticleState;
import it.unibs.pajc.core.PhysicsConstants;
import it.unibs.pajc.core.Vector2D;

import java.util.Map;

public class FreefallMotion implements MotionCalculator {

    private final Vector2D initialPosition;
    private final double gravity;

    public FreefallMotion(Vector2D initialPosition) {
        this(initialPosition, PhysicsConstants.GRAVITY);
    }

    public FreefallMotion(Vector2D initialPosition, double gravity) {
        this.initialPosition = initialPosition;
        this.gravity = gravity;
    }

    @Override
    public ParticleState calculate(Particle particle, double deltaTime, double elapsedTime) {
        double y = initialPosition.y() + 0.5 * gravity * elapsedTime * elapsedTime;
        double vy = gravity * elapsedTime;

        return new ParticleState(
            new Vector2D(initialPosition.x(), y),
            new Vector2D(0, vy),
            new Vector2D(0, gravity)
        );
    }

    @Override
    public Map<String, Double> getParameters() {
        return Map.of(
            "Gravity (m/s^2)", gravity,
            "Initial Height (m)", initialPosition.y()
        );
    }

    @Override
    public void reset() {
        // No state to reset
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.FREEFALL;
    }
}
