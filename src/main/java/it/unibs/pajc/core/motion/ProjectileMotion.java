package it.unibs.pajc.core.motion;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.ParticleState;
import it.unibs.pajc.core.PhysicsConstants;
import it.unibs.pajc.core.Vector2D;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProjectileMotion implements MotionCalculator {

    private final Vector2D initialPosition;
    private final Vector2D initialVelocity;
    private final double gravity;

    public ProjectileMotion(Vector2D initialPosition, Vector2D initialVelocity) {
        this(initialPosition, initialVelocity, PhysicsConstants.GRAVITY);
    }

    public ProjectileMotion(Vector2D initialPosition, Vector2D initialVelocity, double gravity) {
        this.initialPosition = initialPosition;
        this.initialVelocity = initialVelocity;
        this.gravity = gravity;
    }

    @Override
    public ParticleState calculate(Particle particle, double deltaTime, double elapsedTime) {
        // In screen coordinates, Y increases downward, so gravity adds to Y
        // initialVelocity.y() negative = going up, positive = going down
        double x = initialPosition.x() + initialVelocity.x() * elapsedTime;
        double y = initialPosition.y() + initialVelocity.y() * elapsedTime + 0.5 * gravity * elapsedTime * elapsedTime;

        double vx = initialVelocity.x();
        double vy = initialVelocity.y() + gravity * elapsedTime;

        return new ParticleState(
            new Vector2D(x, y),
            new Vector2D(vx, vy),
            new Vector2D(0, gravity)
        );
    }

    @Override
    public Map<String, Double> getParameters() {
        Map<String, Double> params = new LinkedHashMap<>();
        params.put("Initial Vx (m/s)", initialVelocity.x());
        params.put("Initial Vy (m/s)", initialVelocity.y());
        params.put("Gravity (m/s^2)", gravity);
        params.put("Range (m)", calculateRange());
        params.put("Max Height (m)", calculateMaxHeight());
        params.put("Time of Flight (s)", calculateTimeOfFlight());
        return params;
    }

    private double calculateRange() {
        if (gravity == 0) return Double.POSITIVE_INFINITY;
        double timeOfFlight = calculateTimeOfFlight();
        return Math.abs(initialVelocity.x() * timeOfFlight);
    }

    private double calculateMaxHeight() {
        if (gravity >= 0) return initialPosition.y();
        double timeToApex = -initialVelocity.y() / gravity;
        if (timeToApex < 0) return initialPosition.y();
        return initialPosition.y() + initialVelocity.y() * timeToApex + 0.5 * gravity * timeToApex * timeToApex;
    }

    private double calculateTimeOfFlight() {
        if (gravity == 0) return Double.POSITIVE_INFINITY;
        double a = 0.5 * gravity;
        double b = initialVelocity.y();
        double c = initialPosition.y();

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return 0;

        double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);

        if (t1 > 0 && t2 > 0) return Math.min(t1, t2);
        if (t1 > 0) return t1;
        if (t2 > 0) return t2;
        return 0;
    }

    @Override
    public void reset() {
        // No state to reset
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.PROJECTILE;
    }
}
