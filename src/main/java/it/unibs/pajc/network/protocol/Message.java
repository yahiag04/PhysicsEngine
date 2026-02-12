package it.unibs.pajc.network.protocol;

import java.io.Serializable;

public abstract class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MessageType type;
    private final long timestamp;
    private final int senderId;

    protected Message(MessageType type, int senderId) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.senderId = senderId;
    }

    public MessageType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSenderId() {
        return senderId;
    }
}
