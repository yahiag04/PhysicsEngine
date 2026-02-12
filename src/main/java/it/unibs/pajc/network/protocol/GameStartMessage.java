package it.unibs.pajc.network.protocol;

import java.util.List;

public class GameStartMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final List<StateUpdateMessage.PlayerState> initialStates;
    private final double worldWidth;
    private final double worldHeight;

    public GameStartMessage(List<StateUpdateMessage.PlayerState> initialStates,
                           double worldWidth, double worldHeight) {
        super(MessageType.GAME_START, 0);
        this.initialStates = initialStates;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public List<StateUpdateMessage.PlayerState> getInitialStates() {
        return initialStates;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }
}
