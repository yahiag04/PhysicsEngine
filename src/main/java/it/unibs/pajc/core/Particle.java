package it.unibs.pajc.core;

import it.unibs.pajc.core.motion.MotionCalculator;
import it.unibs.pajc.core.motion.MotionType;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.UUID;

public class Particle implements Serializable {

    private final String id;
    private final int playerId;

    private final double mass;
    private final double radius;
    private final SerializableColor color;

    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;

    private transient MotionType motionType;
    private transient MotionCalculator motionCalculator;
    private double elapsedTime;

    public Particle(double mass, double radius, Color color, Vector2D position, Vector2D velocity) {
        this(UUID.randomUUID().toString(), -1, mass, radius, color, position, velocity);
    }

    public Particle(String id, int playerId, double mass, double radius, Color color,
                    Vector2D position, Vector2D velocity) {
        this.id = id;
        this.playerId = playerId;
        this.mass = mass;
        this.radius = radius;
        this.color = new SerializableColor(color);
        this.position = position;
        this.velocity = velocity;
        this.acceleration = Vector2D.ZERO;
        this.elapsedTime = 0;
    }

    public void update(double deltaTime) {
        if (motionCalculator != null) {
            elapsedTime += deltaTime;
            ParticleState newState = motionCalculator.calculate(this, deltaTime, elapsedTime);
            this.position = newState.position();
            this.velocity = newState.velocity();
            this.acceleration = newState.acceleration();
        } else {
            velocity = velocity.add(acceleration.multiply(deltaTime));
            position = position.add(velocity.multiply(deltaTime));
        }
    }

    public void applyForce(Vector2D force) {
        Vector2D accel = force.divide(mass);
        this.acceleration = this.acceleration.add(accel);
    }

    public void applyImpulse(Vector2D impulse) {
        this.velocity = this.velocity.add(impulse.divide(mass));
    }

    public void setMotion(MotionType type, MotionCalculator calculator) {
        this.motionType = type;
        this.motionCalculator = calculator;
        this.elapsedTime = 0;
    }

    public void clearMotion() {
        this.motionType = null;
        this.motionCalculator = null;
    }

    public void resetTime() {
        this.elapsedTime = 0;
        if (motionCalculator != null) {
            motionCalculator.reset();
        }
    }

    public ParticleState getState() {
        return new ParticleState(position, velocity, acceleration);
    }

    public void setState(ParticleState state) {
        this.position = state.position();
        this.velocity = state.velocity();
        this.acceleration = state.acceleration();
    }

    public double kineticEnergy() {
        return 0.5 * mass * velocity.magnitudeSquared();
    }

    public Vector2D momentum() {
        return velocity.multiply(mass);
    }

    public boolean overlaps(Particle other) {
        double dist = position.distanceTo(other.position);
        return dist < (radius + other.radius);
    }

    public double distanceTo(Particle other) {
        return position.distanceTo(other.position);
    }

    // Getters
    public String getId() { return id; }
    public int getPlayerId() { return playerId; }
    public double getMass() { return mass; }
    public double getRadius() { return radius; }
    public Color getColor() { return color.toColor(); }
    public Vector2D getPosition() { return position; }
    public Vector2D getVelocity() { return velocity; }
    public Vector2D getAcceleration() { return acceleration; }
    public MotionType getMotionType() { return motionType; }
    public MotionCalculator getMotionCalculator() { return motionCalculator; }
    public double getElapsedTime() { return elapsedTime; }

    // Setters
    public void setPosition(Vector2D position) { this.position = position; }
    public void setVelocity(Vector2D velocity) { this.velocity = velocity; }
    public void setAcceleration(Vector2D acceleration) { this.acceleration = acceleration; }

    private static class SerializableColor implements Serializable {
        private final double red, green, blue, opacity;

        public SerializableColor(Color color) {
            this.red = color.getRed();
            this.green = color.getGreen();
            this.blue = color.getBlue();
            this.opacity = color.getOpacity();
        }

        public Color toColor() {
            return new Color(red, green, blue, opacity);
        }
    }
}
