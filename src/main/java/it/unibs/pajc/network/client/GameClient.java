package it.unibs.pajc.network.client;

import it.unibs.pajc.core.Vector2D;
import it.unibs.pajc.network.protocol.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class GameClient {

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private int playerId;
    private String playerName;
    private long inputSequence;

    private volatile boolean connected;
    private Thread listenerThread;

    private Consumer<StateUpdateMessage> stateUpdateHandler;
    private Consumer<GameStartMessage> gameStartHandler;
    private Consumer<PlayerLeftMessage> playerLeftHandler;
    private Consumer<String> statusHandler;
    private Runnable disconnectHandler;

    private final BlockingQueue<Message> outgoingMessages;

    public GameClient() {
        this.connected = false;
        this.inputSequence = 0;
        this.outgoingMessages = new LinkedBlockingQueue<>();
    }

    public boolean connect(String host, int port, String playerName, javafx.scene.paint.Color color) {
        try {
            this.playerName = playerName;
            socket = new Socket(host, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            JoinMessage joinMsg = new JoinMessage(playerName, color);
            out.writeObject(joinMsg);
            out.flush();

            Message response = (Message) in.readObject();

            if (response instanceof JoinResponseMessage joinResponse) {
                if (joinResponse.isAccepted()) {
                    this.playerId = joinResponse.getAssignedPlayerId();
                    connected = true;
                    log("Connected as Player " + playerId);
                    return true;
                } else {
                    log("Connection rejected: " + joinResponse.getRejectionReason());
                    socket.close();
                    return false;
                }
            }

            return false;
        } catch (IOException | ClassNotFoundException e) {
            log("Connection failed: " + e.getMessage());
            return false;
        }
    }

    public void startListening() {
        if (!connected) return;

        listenerThread = new Thread(() -> {
            try {
                while (connected && !socket.isClosed()) {
                    Message message = (Message) in.readObject();
                    handleMessage(message);
                }
            } catch (EOFException e) {
                log("Server closed connection");
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    log("Error reading from server: " + e.getMessage());
                }
            } finally {
                disconnect();
            }
        }, "Client-Listener");

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case STATE_UPDATE -> {
                if (stateUpdateHandler != null) {
                    stateUpdateHandler.accept((StateUpdateMessage) message);
                }
            }
            case GAME_START -> {
                if (gameStartHandler != null) {
                    gameStartHandler.accept((GameStartMessage) message);
                }
            }
            case PLAYER_LEFT -> {
                if (playerLeftHandler != null) {
                    playerLeftHandler.accept((PlayerLeftMessage) message);
                }
            }
            case COLLISION_EVENT -> {
                // Handled via StateUpdateMessage
            }
            case PONG -> {
                // Latency measurement response
            }
            default -> log("Unknown message type: " + message.getType());
        }
    }

    public void sendInput(Vector2D direction) {
        if (!connected) return;

        inputSequence++;
        InputMessage input = new InputMessage(playerId, direction, inputSequence);

        try {
            out.writeObject(input);
            out.flush();
            out.reset();
        } catch (IOException e) {
            log("Error sending input: " + e.getMessage());
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
            log("Error disconnecting: " + e.getMessage());
        }

        if (disconnectHandler != null) {
            disconnectHandler.run();
        }

        log("Disconnected from server");
    }

    public void setStateUpdateHandler(Consumer<StateUpdateMessage> handler) {
        this.stateUpdateHandler = handler;
    }

    public void setGameStartHandler(Consumer<GameStartMessage> handler) {
        this.gameStartHandler = handler;
    }

    public void setPlayerLeftHandler(Consumer<PlayerLeftMessage> handler) {
        this.playerLeftHandler = handler;
    }

    public void setStatusHandler(Consumer<String> handler) {
        this.statusHandler = handler;
    }

    public void setDisconnectHandler(Runnable handler) {
        this.disconnectHandler = handler;
    }

    private void log(String message) {
        System.out.println("[Client] " + message);
        if (statusHandler != null) {
            statusHandler.accept(message);
        }
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getPlayerName() {
        return playerName;
    }
}
