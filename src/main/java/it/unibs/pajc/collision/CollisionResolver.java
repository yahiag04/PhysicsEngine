package it.unibs.pajc.collision;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.PhysicsConstants;
import it.unibs.pajc.core.Vector2D;

public class CollisionResolver {

    private final double restitution;

    public CollisionResolver() {
        this(PhysicsConstants.RESTITUTION);
    }

    public CollisionResolver(double restitution) {
        this.restitution = restitution;
    }

    public CollisionEvent resolve(CollisionDetector.CollisionPair pair) {
        Particle a = pair.a();
        Particle b = pair.b();

        // Vector from A to B
        Vector2D delta = b.getPosition().subtract(a.getPosition());
        double distance = delta.magnitude();

        if (distance == 0) {
            // Particles are exactly overlapping, push apart randomly
            delta = new Vector2D(1, 0);
            distance = 1;
        }

        // Normal vector pointing from A to B
        Vector2D normal = delta.divide(distance);

        // Relative velocity of A with respect to B
        Vector2D relativeVelocity = a.getVelocity().subtract(b.getVelocity());

        // Relative velocity along the collision normal
        // Negative means approaching, positive means separating
        double velAlongNormal = relativeVelocity.dot(normal);

        // Calculate impact speed BEFORE any modifications
        double impactSpeed = Math.abs(velAlongNormal);

        // If particles are separating, don't apply impulse but still separate them
        if (velAlongNormal > 0) {
            separateParticles(a, b, normal, pair.penetration());
            return null;
        }

        // Calculate masses
        double massA = a.getMass();
        double massB = b.getMass();
        double totalMass = massA + massB;

        // Elastic collision formula:
        // For perfectly elastic collision (restitution = 1):
        // v1_new = v1 - (2 * m2 / (m1 + m2)) * <v1-v2, n> * n
        // v2_new = v2 + (2 * m1 / (m1 + m2)) * <v1-v2, n> * n

        // With restitution coefficient e:
        // impulse = (1 + e) * velAlongNormal / (1/m1 + 1/m2)

        double impulseMagnitude = -(1 + restitution) * velAlongNormal / (1/massA + 1/massB);

        // Apply impulse to both particles
        Vector2D impulse = normal.multiply(impulseMagnitude);

        // A gets pushed back (opposite to normal)
        Vector2D newVelA = a.getVelocity().add(impulse.divide(massA));
        // B gets pushed forward (along normal)
        Vector2D newVelB = b.getVelocity().subtract(impulse.divide(massB));

        a.setVelocity(newVelA);
        b.setVelocity(newVelB);

        // Now separate particles to prevent overlap
        separateParticles(a, b, normal, pair.penetration());

        return CollisionEvent.create(
            a.getId(),
            b.getId(),
            pair.contactPoint(),
            relativeVelocity,
            impactSpeed
        );
    }

    private void separateParticles(Particle a, Particle b, Vector2D normal, double penetration) {
        if (penetration <= 0) return;

        double massA = a.getMass();
        double massB = b.getMass();
        double totalMass = massA + massB;

        // Separate particles proportionally to their masses
        // Lighter particle moves more
        double separationA = penetration * (massB / totalMass);
        double separationB = penetration * (massA / totalMass);

        // Add small buffer to ensure no overlap
        separationA += 0.5;
        separationB += 0.5;

        // Move A away from B (opposite to normal)
        a.setPosition(a.getPosition().subtract(normal.multiply(separationA)));
        // Move B away from A (along normal)
        b.setPosition(b.getPosition().add(normal.multiply(separationB)));
    }

    public void resolveWallCollision(Particle particle, Vector2D wallNormal,
                                      double width, double height) {
        Vector2D vel = particle.getVelocity();

        // Reflect velocity and apply restitution
        Vector2D newVel = vel.reflect(wallNormal).multiply(restitution);
        particle.setVelocity(newVel);

        // Clamp position to stay within bounds
        Vector2D pos = particle.getPosition();
        double r = particle.getRadius();

        double newX = Math.max(r, Math.min(width - r, pos.x()));
        double newY = Math.max(r, Math.min(height - r, pos.y()));

        particle.setPosition(new Vector2D(newX, newY));
    }
}
