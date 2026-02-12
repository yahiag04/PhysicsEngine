package it.unibs.pajc.network.protocol;

public class PlayerLeftMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final int leftPlayerId;
    private final String playerName;

    public PlayerLeftMessage(int leftPlayerId, String playerName) {
        super(MessageType.PLAYER_LEFT, 0);
        this.leftPlayerId = leftPlayerId;
        this.playerName = playerName;
    }

    public int getLeftPlayerId() {
        return leftPlayerId;
    }

    public String getPlayerName() {
        return playerName;
    }
}
