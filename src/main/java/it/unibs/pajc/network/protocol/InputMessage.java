package it.unibs.pajc.network.protocol;

import it.unibs.pajc.core.Vector2D;

public class InputMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final double dirX;
    private final double dirY;
    private final long inputSequence;

    public InputMessage(int playerId, Vector2D movementDirection, long inputSequence) {
        super(MessageType.PLAYER_INPUT, playerId);
        this.dirX = movementDirection.x();
        this.dirY = movementDirection.y();
        this.inputSequence = inputSequence;
    }

    public Vector2D getMovementDirection() {
        return new Vector2D(dirX, dirY);
    }

    public long getInputSequence() {
        return inputSequence;
    }
}
