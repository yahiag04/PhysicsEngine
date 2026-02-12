package it.unibs.pajc.core;

import it.unibs.pajc.collision.CollisionDetector;
import it.unibs.pajc.collision.CollisionEvent;
import it.unibs.pajc.collision.CollisionResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhysicsWorld {

    private final List<Particle> particles;
    private final CollisionDetector collisionDetector;
    private final CollisionResolver collisionResolver;
    private final List<CollisionEvent> recentCollisions;

    private final double width;
    private final double height;
    private final boolean bounceOffWalls;

    private Vector2D gravity;
    private double damping;

    public PhysicsWorld(double width, double height) {
        this(width, height, true);
    }

    public PhysicsWorld(double width, double height, boolean bounceOffWalls) {
        this.particles = new CopyOnWriteArrayList<>();
        this.collisionDetector = new CollisionDetector();
        this.collisionResolver = new CollisionResolver();
        this.recentCollisions = new ArrayList<>();
        this.width = width;
        this.height = height;
        this.bounceOffWalls = bounceOffWalls;
        this.gravity = Vector2D.ZERO;
        this.damping = PhysicsConstants.DEFAULT_DAMPING;
    }

    public void update(double deltaTime) {
        recentCollisions.clear();

        for (Particle particle : particles) {
            if (particle.getMotionCalculator() == null) {
                if (gravity.magnitudeSquared() > 0) {
                    particle.setAcceleration(gravity);
                }

                particle.update(deltaTime);

                if (damping < 1.0) {
                    particle.setVelocity(particle.getVelocity().multiply(damping));
                }
            } else {
                particle.update(deltaTime);
            }
        }

        List<CollisionDetector.CollisionPair> collisions = collisionDetector.detectCollisions(particles);
        for (CollisionDetector.CollisionPair pair : collisions) {
            CollisionEvent event = collisionResolver.resolve(pair);
            if (event != null) {
                recentCollisions.add(event);
            }
        }

        if (bounceOffWalls) {
            handleBoundaries();
        }
    }

    private void handleBoundaries() {
        for (Particle particle : particles) {
            if (collisionDetector.checkWallCollision(particle, width, height)) {
                Vector2D wallNormal = collisionDetector.getWallNormal(particle, width, height);
                if (wallNormal.magnitudeSquared() > 0) {
                    collisionResolver.resolveWallCollision(particle, wallNormal, width, height);
                }
            }
        }
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public void removeParticle(String particleId) {
        particles.removeIf(p -> p.getId().equals(particleId));
    }

    public Particle getParticle(String particleId) {
        return particles.stream()
            .filter(p -> p.getId().equals(particleId))
            .findFirst()
            .orElse(null);
    }

    public Particle getParticleByPlayerId(int playerId) {
        return particles.stream()
            .filter(p -> p.getPlayerId() == playerId)
            .findFirst()
            .orElse(null);
    }

    public List<Particle> getParticles() {
        return new ArrayList<>(particles);
    }

    public List<CollisionEvent> getRecentCollisions() {
        return new ArrayList<>(recentCollisions);
    }

    public void clear() {
        particles.clear();
        recentCollisions.clear();
    }

    public void setGravity(Vector2D gravity) {
        this.gravity = gravity;
    }

    public void setDamping(double damping) {
        this.damping = damping;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public int getParticleCount() {
        return particles.size();
    }
}
