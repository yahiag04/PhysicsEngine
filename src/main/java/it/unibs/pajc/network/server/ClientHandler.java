package it.unibs.pajc.network.server;

import it.unibs.pajc.network.protocol.*;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final GameServer server;
    private final int playerId;
    private final String playerName;

    private volatile boolean connected;

    public ClientHandler(Socket socket, GameServer server, int playerId, String playerName,
                        ObjectInputStream in, ObjectOutputStream out) {
        this.socket = socket;
        this.server = server;
        this.playerId = playerId;
        this.playerName = playerName;
        this.in = in;
        this.out = out;
        this.connected = true;
    }

    @Override
    public void run() {
        try {
            while (connected && !socket.isClosed()) {
                Message message = (Message) in.readObject();
                processMessage(message);
            }
        } catch (EOFException e) {
            System.out.println("Client " + playerId + " disconnected");
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                System.err.println("Error reading from client " + playerId + ": " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    private void processMessage(Message message) {
        switch (message.getType()) {
            case PLAYER_INPUT -> {
                InputMessage input = (InputMessage) message;
                server.handlePlayerInput(playerId, input);
            }
            case PING -> {
                sendMessage(new PongMessage());
            }
            default -> {
                System.out.println("Unknown message type from client " + playerId + ": " + message.getType());
            }
        }
    }

    public synchronized void sendMessage(Message message) {
        if (!connected || socket.isClosed()) return;

        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("Error sending to client " + playerId + ": " + e.getMessage());
            disconnect();
        }
    }

    public void disconnect() {
        if (!connected) return;
        connected = false;

        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection for client " + playerId);
        }

        server.removePlayer(playerId);
    }

    public boolean isConnected() {
        return connected && !socket.isClosed();
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    private static class PongMessage extends Message {
        private static final long serialVersionUID = 1L;

        public PongMessage() {
            super(MessageType.PONG, 0);
        }
    }
}
