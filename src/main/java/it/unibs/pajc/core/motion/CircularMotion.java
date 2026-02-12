package it.unibs.pajc.core.motion;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.ParticleState;
import it.unibs.pajc.core.Vector2D;

import java.util.LinkedHashMap;
import java.util.Map;

public class CircularMotion implements MotionCalculator {

    private final Vector2D center;
    private final double radius;
    private final double angularVelocity;
    private final double initialPhase;

    public CircularMotion(Vector2D center, double radius, double angularVelocity) {
        this(center, radius, angularVelocity, 0);
    }

    public CircularMotion(Vector2D center, double radius, double angularVelocity, double initialPhase) {
        this.center = center;
        this.radius = radius;
        this.angularVelocity = angularVelocity;
        this.initialPhase = initialPhase;
    }

    @Override
    public ParticleState calculate(Particle particle, double deltaTime, double elapsedTime) {
        double angle = angularVelocity * elapsedTime + initialPhase;

        double x = center.x() + radius * Math.cos(angle);
        double y = center.y() + radius * Math.sin(angle);

        double vx = -radius * angularVelocity * Math.sin(angle);
        double vy = radius * angularVelocity * Math.cos(angle);

        double ax = -radius * angularVelocity * angularVelocity * Math.cos(angle);
        double ay = -radius * angularVelocity * angularVelocity * Math.sin(angle);

        return new ParticleState(
            new Vector2D(x, y),
            new Vector2D(vx, vy),
            new Vector2D(ax, ay)
        );
    }

    @Override
    public Map<String, Double> getParameters() {
        double period = 2 * Math.PI / Math.abs(angularVelocity);
        double frequency = 1 / period;
        double tangentialSpeed = Math.abs(angularVelocity) * radius;
        double centripetalAccel = angularVelocity * angularVelocity * radius;

        Map<String, Double> params = new LinkedHashMap<>();
        params.put("Radius (m)", radius);
        params.put("Angular Vel (rad/s)", angularVelocity);
        params.put("Period (s)", period);
        params.put("Frequency (Hz)", frequency);
        params.put("Tangential Speed (m/s)", tangentialSpeed);
        params.put("Centripetal Accel (m/s^2)", centripetalAccel);
        return params;
    }

    @Override
    public void reset() {
        // No state to reset
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.CIRCULAR;
    }

    public Vector2D getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
