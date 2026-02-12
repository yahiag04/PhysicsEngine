package it.unibs.pajc.collision;

import it.unibs.pajc.core.Vector2D;

import java.io.Serializable;

public record CollisionEvent(
    String particleIdA,
    String particleIdB,
    Vector2D contactPoint,
    Vector2D relativeVelocity,
    double impactSpeed,
    long timestamp
) implements Serializable {

    public static CollisionEvent create(String idA, String idB, Vector2D contact,
                                        Vector2D relVel, double impact) {
        return new CollisionEvent(idA, idB, contact, relVel, impact, System.currentTimeMillis());
    }
}
