package it.unibs.pajc.network.server;

import it.unibs.pajc.core.PhysicsConstants;
import it.unibs.pajc.network.protocol.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class GameServer {

    private ServerSocket serverSocket;
    private final Map<Integer, ClientHandler> clients;
    private final ServerGameState gameState;
    private final ExecutorService executor;

    private volatile boolean running;
    private Thread acceptThread;
    private Thread gameLoopThread;

    private Consumer<String> statusCallback;

    public GameServer() {
        this.clients = new ConcurrentHashMap<>();
        this.gameState = new ServerGameState();
        this.executor = Executors.newCachedThreadPool();
        this.running = false;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        log("Server started on port " + port);

        acceptThread = new Thread(this::acceptClients, "Accept-Thread");
        acceptThread.start();
    }

    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleNewConnection(clientSocket);
            } catch (SocketException e) {
                if (running) {
                    log("Socket exception: " + e.getMessage());
                }
            } catch (IOException e) {
                if (running) {
                    log("Error accepting client: " + e.getMessage());
                }
            }
        }
    }

    private void handleNewConnection(Socket socket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Message message = (Message) in.readObject();

            if (message.getType() == MessageType.JOIN_REQUEST) {
                JoinMessage joinMsg = (JoinMessage) message;

                if (clients.size() >= PhysicsConstants.MAX_PLAYERS) {
                    out.writeObject(JoinResponseMessage.reject("Server is full"));
                    out.flush();
                    socket.close();
                    return;
                }

                if (gameState.isGameStarted()) {
                    out.writeObject(JoinResponseMessage.reject("Game already in progress"));
                    out.flush();
                    socket.close();
                    return;
                }

                int playerId = gameState.addPlayer(joinMsg.getPlayerName(), joinMsg.getPreferredColor());

                out.writeObject(JoinResponseMessage.accept(playerId, clients.size()));
                out.flush();

                ClientHandler handler = new ClientHandler(socket, this, playerId, joinMsg.getPlayerName(), in, out);
                clients.put(playerId, handler);

                executor.submit(handler);

                log("Player " + joinMsg.getPlayerName() + " joined (ID: " + playerId + ")");
                broadcastPlayerCount();
            }
        } catch (IOException | ClassNotFoundException e) {
            log("Error handling new connection: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    public void startGame() {
        if (clients.isEmpty()) {
            log("Cannot start game with no players");
            return;
        }

        gameState.startGame();

        GameStartMessage startMsg = new GameStartMessage(
            gameState.getPlayerStates(),
            ServerGameState.WORLD_WIDTH,
            ServerGameState.WORLD_HEIGHT
        );
        broadcast(startMsg);

        gameLoopThread = new Thread(this::gameLoop, "Game-Loop");
        gameLoopThread.start();

        log("Game started with " + clients.size() + " players");
    }

    private void gameLoop() {
        long lastTime = System.nanoTime();
        double accumulator = 0;
        double tickTime = 1.0 / PhysicsConstants.TICK_RATE;

        while (running && gameState.isGameStarted()) {
            long currentTime = System.nanoTime();
            double frameTime = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;
            accumulator += frameTime;

            while (accumulator >= tickTime) {
                gameState.update(tickTime);
                accumulator -= tickTime;
            }

            broadcastState();

            try {
                Thread.sleep(Math.max(1, (long) ((tickTime - frameTime) * 1000)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void handlePlayerInput(int playerId, InputMessage input) {
        gameState.applyInput(playerId, input.getMovementDirection(), input.getInputSequence());
    }

    public void removePlayer(int playerId) {
        ClientHandler handler = clients.remove(playerId);
        if (handler != null) {
            String playerName = handler.getPlayerName();
            gameState.removePlayer(playerId);

            broadcast(new PlayerLeftMessage(playerId, playerName));
            log("Player " + playerName + " left");
            broadcastPlayerCount();
        }
    }

    private void broadcastState() {
        StateUpdateMessage update = new StateUpdateMessage(
            gameState.getServerTick(),
            gameState.getPlayerStates(),
            gameState.getPhysicsWorld().getRecentCollisions()
        );
        broadcast(update);
    }

    private void broadcast(Message message) {
        for (ClientHandler handler : clients.values()) {
            if (handler.isConnected()) {
                handler.sendMessage(message);
            }
        }
    }

    private void broadcastPlayerCount() {
        log("Players: " + clients.size() + "/" + PhysicsConstants.MAX_PLAYERS);
    }

    public void stop() {
        running = false;

        for (ClientHandler handler : clients.values()) {
            handler.disconnect();
        }
        clients.clear();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error closing server socket: " + e.getMessage());
        }

        executor.shutdownNow();

        log("Server stopped");
    }

    public void setStatusCallback(Consumer<String> callback) {
        this.statusCallback = callback;
    }

    private void log(String message) {
        System.out.println("[Server] " + message);
        if (statusCallback != null) {
            statusCallback.accept(message);
        }
    }

    public int getPlayerCount() {
        return clients.size();
    }

    public boolean isRunning() {
        return running;
    }

    public ServerGameState getGameState() {
        return gameState;
    }
}
