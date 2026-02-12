package it.unibs.pajc.core.motion;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.ParticleState;
import it.unibs.pajc.core.PhysicsConstants;
import it.unibs.pajc.core.Vector2D;

import java.util.LinkedHashMap;
import java.util.Map;

public class PendulumMotion implements MotionCalculator {

    private final Vector2D pivotPoint;
    private final double length;
    private final double maxAngle;
    private final double gravity;

    private double currentAngle;
    private double currentAngularVelocity;
    private boolean initialized;

    public PendulumMotion(Vector2D pivotPoint, double length, double maxAngleDegrees) {
        this(pivotPoint, length, maxAngleDegrees, PhysicsConstants.GRAVITY);
    }

    public PendulumMotion(Vector2D pivotPoint, double length, double maxAngleDegrees, double gravity) {
        this.pivotPoint = pivotPoint;
        this.length = length;
        this.maxAngle = Math.toRadians(maxAngleDegrees);
        this.gravity = gravity;
        reset();
    }

    @Override
    public ParticleState calculate(Particle particle, double deltaTime, double elapsedTime) {
        if (!initialized) {
            currentAngle = maxAngle;
            currentAngularVelocity = 0;
            initialized = true;
        }

        double theta, omega, alpha;

        if (Math.abs(maxAngle) < Math.toRadians(15)) {
            double w = Math.sqrt(gravity / length);
            theta = maxAngle * Math.cos(w * elapsedTime);
            omega = -maxAngle * w * Math.sin(w * elapsedTime);
            alpha = -maxAngle * w * w * Math.cos(w * elapsedTime);
        } else {
            integrateRK4(deltaTime);
            theta = currentAngle;
            omega = currentAngularVelocity;
            alpha = -(gravity / length) * Math.sin(theta);
        }

        double x = pivotPoint.x() + length * Math.sin(theta);
        double y = pivotPoint.y() + length * Math.cos(theta);

        double vx = length * omega * Math.cos(theta);
        double vy = -length * omega * Math.sin(theta);

        double ax = length * alpha * Math.cos(theta) - length * omega * omega * Math.sin(theta);
        double ay = -length * alpha * Math.sin(theta) - length * omega * omega * Math.cos(theta);

        return new ParticleState(
            new Vector2D(x, y),
            new Vector2D(vx, vy),
            new Vector2D(ax, ay)
        );
    }

    private void integrateRK4(double dt) {
        double g_over_L = gravity / length;

        double k1_theta = currentAngularVelocity;
        double k1_omega = -g_over_L * Math.sin(currentAngle);

        double k2_theta = currentAngularVelocity + 0.5 * dt * k1_omega;
        double k2_omega = -g_over_L * Math.sin(currentAngle + 0.5 * dt * k1_theta);

        double k3_theta = currentAngularVelocity + 0.5 * dt * k2_omega;
        double k3_omega = -g_over_L * Math.sin(currentAngle + 0.5 * dt * k2_theta);

        double k4_theta = currentAngularVelocity + dt * k3_omega;
        double k4_omega = -g_over_L * Math.sin(currentAngle + dt * k3_theta);

        currentAngle += (dt / 6) * (k1_theta + 2*k2_theta + 2*k3_theta + k4_theta);
        currentAngularVelocity += (dt / 6) * (k1_omega + 2*k2_omega + 2*k3_omega + k4_omega);
    }

    @Override
    public Map<String, Double> getParameters() {
        double period = 2 * Math.PI * Math.sqrt(length / gravity);
        double frequency = 1 / period;

        Map<String, Double> params = new LinkedHashMap<>();
        params.put("Length (m)", length);
        params.put("Max Angle (deg)", Math.toDegrees(maxAngle));
        params.put("Period (s)", period);
        params.put("Frequency (Hz)", frequency);
        params.put("Gravity (m/s^2)", gravity);
        return params;
    }

    @Override
    public void reset() {
        currentAngle = maxAngle;
        currentAngularVelocity = 0;
        initialized = false;
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.PENDULUM;
    }

    public Vector2D getPivotPoint() {
        return pivotPoint;
    }

    public double getLength() {
        return length;
    }
}
