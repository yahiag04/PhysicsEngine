package it.unibs.pajc.network.protocol;

public enum MessageType {
    JOIN_REQUEST,
    JOIN_RESPONSE,
    PLAYER_INPUT,
    STATE_UPDATE,
    COLLISION_EVENT,
    PLAYER_LEFT,
    GAME_START,
    PING,
    PONG
}
