package it.unibs.pajc.core;

import java.io.Serializable;

public record ParticleState(
    Vector2D position,
    Vector2D velocity,
    Vector2D acceleration
) implements Serializable {

    public static ParticleState atRest(Vector2D position) {
        return new ParticleState(position, Vector2D.ZERO, Vector2D.ZERO);
    }

    public static ParticleState withVelocity(Vector2D position, Vector2D velocity) {
        return new ParticleState(position, velocity, Vector2D.ZERO);
    }
}
