package it.unibs.pajc.core;

public final class PhysicsConstants {

    private PhysicsConstants() {}

    public static final double GRAVITY = 9.81;
    public static final double DEFAULT_DAMPING = 0.99;
    public static final double COLLISION_THRESHOLD = 0.001;
    public static final double MIN_VELOCITY = 0.01;
    public static final double TIME_SCALE = 1.0;
    public static final int PIXELS_PER_METER = 50;

    public static final double DEFAULT_PARTICLE_MASS = 1.0;
    public static final double DEFAULT_PARTICLE_RADIUS = 20.0;

    public static final int SERVER_PORT = 5555;
    public static final int TICK_RATE = 60;
    public static final int MAX_PLAYERS = 4;

    public static final double PLAYER_MOVE_FORCE = 500.0;
    public static final double RESTITUTION = 1.0;
}
