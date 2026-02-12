package it.unibs.pajc.network.protocol;

import it.unibs.pajc.collision.CollisionEvent;
import it.unibs.pajc.core.Vector2D;

import java.io.Serializable;
import java.util.List;

public class StateUpdateMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final long serverTick;
    private final List<PlayerState> playerStates;
    private final List<CollisionEvent> recentCollisions;

    public StateUpdateMessage(long serverTick, List<PlayerState> playerStates,
                              List<CollisionEvent> recentCollisions) {
        super(MessageType.STATE_UPDATE, 0);
        this.serverTick = serverTick;
        this.playerStates = playerStates;
        this.recentCollisions = recentCollisions;
    }

    public long getServerTick() {
        return serverTick;
    }

    public List<PlayerState> getPlayerStates() {
        return playerStates;
    }

    public List<CollisionEvent> getRecentCollisions() {
        return recentCollisions;
    }

    public static class PlayerState implements Serializable {
        private static final long serialVersionUID = 1L;

        public final int playerId;
        public final String particleId;
        public final double posX, posY;
        public final double velX, velY;
        public final double radius;
        public final double red, green, blue;
        public final long lastProcessedInput;

        public PlayerState(int playerId, String particleId, Vector2D position, Vector2D velocity,
                          double radius, double red, double green, double blue, long lastProcessedInput) {
            this.playerId = playerId;
            this.particleId = particleId;
            this.posX = position.x();
            this.posY = position.y();
            this.velX = velocity.x();
            this.velY = velocity.y();
            this.radius = radius;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.lastProcessedInput = lastProcessedInput;
        }

        public Vector2D getPosition() {
            return new Vector2D(posX, posY);
        }

        public Vector2D getVelocity() {
            return new Vector2D(velX, velY);
        }
    }
}
