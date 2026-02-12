package it.unibs.pajc.network.protocol;

public class JoinResponseMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final boolean accepted;
    private final int assignedPlayerId;
    private final String rejectionReason;
    private final int currentPlayerCount;

    public JoinResponseMessage(boolean accepted, int assignedPlayerId,
                               String rejectionReason, int currentPlayerCount) {
        super(MessageType.JOIN_RESPONSE, 0);
        this.accepted = accepted;
        this.assignedPlayerId = assignedPlayerId;
        this.rejectionReason = rejectionReason;
        this.currentPlayerCount = currentPlayerCount;
    }

    public static JoinResponseMessage accept(int playerId, int playerCount) {
        return new JoinResponseMessage(true, playerId, null, playerCount);
    }

    public static JoinResponseMessage reject(String reason) {
        return new JoinResponseMessage(false, -1, reason, 0);
    }

    public boolean isAccepted() {
        return accepted;
    }

    public int getAssignedPlayerId() {
        return assignedPlayerId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public int getCurrentPlayerCount() {
        return currentPlayerCount;
    }
}
