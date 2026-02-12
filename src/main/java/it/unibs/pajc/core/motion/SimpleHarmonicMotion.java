package it.unibs.pajc.core.motion;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.ParticleState;
import it.unibs.pajc.core.Vector2D;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleHarmonicMotion implements MotionCalculator {

    private final Vector2D equilibriumPosition;
    private final Vector2D direction;
    private final double amplitude;
    private final double angularFrequency;
    private final double initialPhase;

    public SimpleHarmonicMotion(Vector2D equilibriumPosition, double amplitude, double angularFrequency) {
        this(equilibriumPosition, Vector2D.UNIT_X, amplitude, angularFrequency, 0);
    }

    public SimpleHarmonicMotion(Vector2D equilibriumPosition, Vector2D direction,
                                double amplitude, double angularFrequency, double initialPhase) {
        this.equilibriumPosition = equilibriumPosition;
        this.direction = direction.normalize();
        this.amplitude = amplitude;
        this.angularFrequency = angularFrequency;
        this.initialPhase = initialPhase;
    }

    @Override
    public ParticleState calculate(Particle particle, double deltaTime, double elapsedTime) {
        double phase = angularFrequency * elapsedTime + initialPhase;

        double displacement = amplitude * Math.cos(phase);
        double velocity = -amplitude * angularFrequency * Math.sin(phase);
        double acceleration = -amplitude * angularFrequency * angularFrequency * Math.cos(phase);

        Vector2D pos = equilibriumPosition.add(direction.multiply(displacement));
        Vector2D vel = direction.multiply(velocity);
        Vector2D acc = direction.multiply(acceleration);

        return new ParticleState(pos, vel, acc);
    }

    @Override
    public Map<String, Double> getParameters() {
        double period = 2 * Math.PI / angularFrequency;
        double frequency = 1 / period;
        double maxVelocity = amplitude * angularFrequency;
        double maxAcceleration = amplitude * angularFrequency * angularFrequency;

        Map<String, Double> params = new LinkedHashMap<>();
        params.put("Amplitude (m)", amplitude);
        params.put("Angular Freq (rad/s)", angularFrequency);
        params.put("Period (s)", period);
        params.put("Frequency (Hz)", frequency);
        params.put("Max Velocity (m/s)", maxVelocity);
        params.put("Max Accel (m/s^2)", maxAcceleration);
        return params;
    }

    @Override
    public void reset() {
        // No state to reset
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.SIMPLE_HARMONIC;
    }

    public Vector2D getEquilibriumPosition() {
        return equilibriumPosition;
    }

    public double getAmplitude() {
        return amplitude;
    }
}
