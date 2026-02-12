package it.unibs.pajc.network.server;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.PhysicsConstants;
import it.unibs.pajc.core.PhysicsWorld;
import it.unibs.pajc.core.Vector2D;
import it.unibs.pajc.network.protocol.StateUpdateMessage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGameState {

    private final PhysicsWorld physicsWorld;
    private final Map<Integer, PlayerInfo> players;
    private final Map<Integer, Long> lastProcessedInput;

    private long serverTick;
    private boolean gameStarted;

    public static final double WORLD_WIDTH = 1000;
    public static final double WORLD_HEIGHT = 700;

    public ServerGameState() {
        this.physicsWorld = new PhysicsWorld(WORLD_WIDTH, WORLD_HEIGHT, true);
        this.players = new ConcurrentHashMap<>();
        this.lastProcessedInput = new ConcurrentHashMap<>();
        this.serverTick = 0;
        this.gameStarted = false;
    }

    public synchronized int addPlayer(String name, Color color) {
        int playerId = players.size() + 1;

        Vector2D spawnPosition = getSpawnPosition(playerId);

        Particle particle = new Particle(
            "player-" + playerId,
            playerId,
            PhysicsConstants.DEFAULT_PARTICLE_MASS,
            PhysicsConstants.DEFAULT_PARTICLE_RADIUS,
            color,
            spawnPosition,
            Vector2D.ZERO
        );

        PlayerInfo info = new PlayerInfo(playerId, name, particle);
        players.put(playerId, info);
        physicsWorld.addParticle(particle);
        lastProcessedInput.put(playerId, 0L);

        return playerId;
    }

    private Vector2D getSpawnPosition(int playerId) {
        // Spawn players close together in the center area, spaced to avoid overlap
        double centerX = WORLD_WIDTH / 2;
        double centerY = WORLD_HEIGHT / 2;
        double spacing = 80; // Enough space to avoid overlap (radius is ~20)

        return switch (playerId) {
            case 1 -> new Vector2D(centerX - spacing, centerY - spacing);
            case 2 -> new Vector2D(centerX + spacing, centerY - spacing);
            case 3 -> new Vector2D(centerX - spacing, centerY + spacing);
            case 4 -> new Vector2D(centerX + spacing, centerY + spacing);
            default -> new Vector2D(centerX, centerY);
        };
    }

    public synchronized void removePlayer(int playerId) {
        PlayerInfo info = players.remove(playerId);
        if (info != null) {
            physicsWorld.removeParticle(info.particle.getId());
        }
        lastProcessedInput.remove(playerId);
    }

    public void applyInput(int playerId, Vector2D direction, long inputSequence) {
        PlayerInfo info = players.get(playerId);
        if (info == null) return;

        Particle particle = info.particle;
        if (direction.magnitudeSquared() > 0) {
            // Apply velocity boost - accelerate in the direction pressed
            double acceleration = 15.0; // Units per frame
            Vector2D velocityBoost = direction.normalize().multiply(acceleration);
            Vector2D newVel = particle.getVelocity().add(velocityBoost);

            // Cap max speed at 600 - allows building up significant momentum
            double maxSpeed = 600;
            if (newVel.magnitude() > maxSpeed) {
                newVel = newVel.normalize().multiply(maxSpeed);
            }
            particle.setVelocity(newVel);
        }

        lastProcessedInput.put(playerId, inputSequence);
    }

    public void update(double deltaTime) {
        if (!gameStarted) return;

        serverTick++;

        // Lower damping = velocities maintained longer = more noticeable speed differences
        physicsWorld.setDamping(0.98);
        physicsWorld.update(deltaTime);
    }

    public List<StateUpdateMessage.PlayerState> getPlayerStates() {
        List<StateUpdateMessage.PlayerState> states = new ArrayList<>();

        for (PlayerInfo info : players.values()) {
            Particle p = info.particle;
            Color c = p.getColor();

            states.add(new StateUpdateMessage.PlayerState(
                info.playerId,
                p.getId(),
                p.getPosition(),
                p.getVelocity(),
                p.getRadius(),
                c.getRed(),
                c.getGreen(),
                c.getBlue(),
                lastProcessedInput.getOrDefault(info.playerId, 0L)
            ));
        }

        return states;
    }

    public void startGame() {
        this.gameStarted = true;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public long getServerTick() {
        return serverTick;
    }

    public PhysicsWorld getPhysicsWorld() {
        return physicsWorld;
    }

    public PlayerInfo getPlayerInfo(int playerId) {
        return players.get(playerId);
    }

    public Map<Integer, PlayerInfo> getPlayers() {
        return players;
    }

    public static class PlayerInfo {
        public final int playerId;
        public final String name;
        public final Particle particle;

        public PlayerInfo(int playerId, String name, Particle particle) {
            this.playerId = playerId;
            this.name = name;
            this.particle = particle;
        }
    }
}
