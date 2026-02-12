package it.unibs.pajc.collision;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class CollisionDetector {

    public record CollisionPair(
        Particle a,
        Particle b,
        Vector2D contactPoint,
        Vector2D normal,
        double penetration
    ) {}

    public List<CollisionPair> detectCollisions(List<Particle> particles) {
        List<CollisionPair> collisions = new ArrayList<>();

        for (int i = 0; i < particles.size(); i++) {
            for (int j = i + 1; j < particles.size(); j++) {
                Particle a = particles.get(i);
                Particle b = particles.get(j);

                CollisionPair collision = checkCollision(a, b);
                if (collision != null) {
                    collisions.add(collision);
                }
            }
        }

        return collisions;
    }

    private CollisionPair checkCollision(Particle a, Particle b) {
        Vector2D posA = a.getPosition();
        Vector2D posB = b.getPosition();

        double distance = posA.distanceTo(posB);
        double minDistance = a.getRadius() + b.getRadius();

        if (distance < minDistance && distance > 0) {
            Vector2D normal = posB.subtract(posA).normalize();

            Vector2D contactPoint = posA.add(normal.multiply(a.getRadius()));

            double penetration = minDistance - distance;

            return new CollisionPair(a, b, contactPoint, normal, penetration);
        }

        return null;
    }

    public boolean checkWallCollision(Particle particle, double width, double height) {
        Vector2D pos = particle.getPosition();
        double r = particle.getRadius();

        return pos.x() - r < 0 || pos.x() + r > width ||
               pos.y() - r < 0 || pos.y() + r > height;
    }

    public Vector2D getWallNormal(Particle particle, double width, double height) {
        Vector2D pos = particle.getPosition();
        double r = particle.getRadius();

        if (pos.x() - r < 0) return new Vector2D(1, 0);
        if (pos.x() + r > width) return new Vector2D(-1, 0);
        if (pos.y() - r < 0) return new Vector2D(0, 1);
        if (pos.y() + r > height) return new Vector2D(0, -1);

        return Vector2D.ZERO;
    }
}
