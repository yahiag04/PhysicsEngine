package it.unibs.pajc.network.protocol;

import javafx.scene.paint.Color;

public class JoinMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final String playerName;
    private final double red, green, blue;

    public JoinMessage(String playerName, Color preferredColor) {
        super(MessageType.JOIN_REQUEST, -1);
        this.playerName = playerName;
        this.red = preferredColor.getRed();
        this.green = preferredColor.getGreen();
        this.blue = preferredColor.getBlue();
    }

    public String getPlayerName() {
        return playerName;
    }

    public Color getPreferredColor() {
        return Color.color(red, green, blue);
    }
}
