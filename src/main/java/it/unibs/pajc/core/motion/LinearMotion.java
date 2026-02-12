package it.unibs.pajc.core.motion;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.ParticleState;
import it.unibs.pajc.core.Vector2D;

import java.util.Map;

public class LinearMotion implements MotionCalculator {

    private final Vector2D initialPosition;
    private final Vector2D velocity;

    public LinearMotion(Vector2D initialPosition, Vector2D velocity) {
        this.initialPosition = initialPosition;
        this.velocity = velocity;
    }

    @Override
    public ParticleState calculate(Particle particle, double deltaTime, double elapsedTime) {
        Vector2D position = initialPosition.add(velocity.multiply(elapsedTime));
        return new ParticleState(position, velocity, Vector2D.ZERO);
    }

    @Override
    public Map<String, Double> getParameters() {
        return Map.of(
            "Speed (m/s)", velocity.magnitude(),
            "Direction (deg)", Math.toDegrees(velocity.angle()),
            "Vx (m/s)", velocity.x(),
            "Vy (m/s)", velocity.y()
        );
    }

    @Override
    public void reset() {
        // No state to reset for linear motion
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.LINEAR;
    }
}
