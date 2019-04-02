package mijnlieff.client;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import mijnlieff.client.board.BoardSetting;
import mijnlieff.client.game.Player;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Handles the connection with the server
 */
public class Connection{

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private Player player;
    private WaitingState state;
    private boolean enqueued;

    private Timeline playerRefresher;
    private ConnectionListener listener;

    /**
     * Starts the connection with the specified hostName and portNumber.
     * @param hostName the host name to create this connection
     * @param portNumber the port number to create this connection
     * @throws IOException if the socket could not be created
     * @see Socket
     */
    public void start(String hostName, int portNumber) throws IOException {
        System.out.println("Opening connection: " + hostName + " " + portNumber);
        socket = new Socket(hostName, portNumber);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        player = new Player("Viewer", null);
        state = WaitingState.IDLE;
        enqueued = false;
    }

    private void startWatchingPlayerList() {
        playerRefresher = new Timeline(new KeyFrame(Duration.seconds(1), e -> requestPlayerList()));
        playerRefresher.setCycleCount(Timeline.INDEFINITE);
        playerRefresher.play();
    }

    public Player getPlayer() {
        return player;
    }

    public void setListener(ConnectionListener listener) {
        this.listener = listener;
    }

    /**
     * Lets the server know who the player is.
     * No authentication is required.
     * Starts a thread to listen to any responses from this point.
     * @param username the username to pass to the server
     * @see Thread
     */
    public void identify(String username) {
        player.setUsername(username);
        new Thread(this::listen).start();
        out.println("I " + username);
        state = WaitingState.IDENTIFY;

        startWatchingPlayerList();
    }

    /**
     * Requests the player list from the server.
     */
    private void requestPlayerList() {
        if(state != WaitingState.IDLE) return;

        out.println("W");
        state = WaitingState.PLAYERLIST;
    }

    /**
     * Marks the player on the global player list, available for any opponent to start a game.
     */
    public void enqueue() {
        if(state != WaitingState.IDLE) return;

        out.println("P");
        state = WaitingState.IDLE;
        enqueued = true;
    }

    /**
     * Removes the player from the global player list.
     */
    public void dequeue() {
        if(state != WaitingState.IDLE) return;

        out.println("R");
        state = WaitingState.DEQUEUED;
    }

    /**
     * Selects the opponent from the queued player list to play against.
     * @param opponentName the name of the opponent that is used to start the game.
     */
    public void selectOpponent(String opponentName) {
        if(state != WaitingState.IDLE) return;

        // Make sure the player is dequeued before we try to select an opponent
        if(enqueued) dequeue();

        out.println("C "+opponentName);
        state = WaitingState.OPPONENT;
    }

    public void sendBoard(BoardSetting boardSetting) {
        if(state != WaitingState.IDLE) return;

        out.println("X " + boardSetting.toString());
        state = WaitingState.GAME;
        System.out.println("I'm not in gaming mode xD");
    }

    public void sendMove(String encodedMove) {
        out.println(encodedMove);
    }

    public String viewNext() throws IOException {
        out.println("X");
        return in.readLine();
    }

    /**
     * Marks the state to wait for the opponent to choose a board.
     */
    public void waitForBoard() {
        if(state != WaitingState.IDLE) return;

        state = WaitingState.BOARD;
        System.out.println("I'm not in gaming mode xD");
    }

    /**
     * Listens to the server for any responses.
     * When it gets a response it will call the responsible method in {@link ConnectionListener}.
     */
    private void listen() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                if(response.equals("Q")) {
                    System.err.println("Server or opponent decided to close the connection...");
                    Platform.exit();
                } else if(state == WaitingState.IDLE) {
                    if(enqueued) {
                        initializeGame(response);
                    }

                } else if(state == WaitingState.IDENTIFY) {
                    boolean success = response.equals("+");
                    Platform.runLater(() -> listener.identified(success));
                    state = WaitingState.IDLE;

                } else if(state == WaitingState.PLAYERLIST) {
                    ArrayList<String> playerNames = new ArrayList<>();
                    while (response.startsWith("+") && response.length() > 2) {
                        playerNames.add(response.substring(2));
                        response = in.readLine();
                    }
                    Platform.runLater(() -> listener.updatePlayerList(playerNames));
                    state = WaitingState.IDLE;

                } else if(state == WaitingState.OPPONENT) {
                    if(response.startsWith("+")) {
                        if(response.length() == 1) {
                            // This response comes because we first dequeued ourselves
                            // Don't change the state, but skip ahead
                            enqueued = false;
                        } else {
                            initializeGame(response);
                        }
                    }

                } else if(state == WaitingState.DEQUEUED) {
                    boolean success = response.equals("+");
                    state = WaitingState.IDLE;
                    enqueued = !success;

                } else if(state == WaitingState.BOARD) {
                    BoardSetting boardSetting = new BoardSetting(response.substring(2));
                    Platform.runLater(() -> listener.boardEstablished(boardSetting));
                    state = WaitingState.GAME;

                } else if(state == WaitingState.GAME) {
                    System.out.println("Got a response: " + response);
                    if(response.length() == 9 ) {
                        String finalResponse = response;
                        Platform.runLater(() -> listener.receivedMove(finalResponse));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Closed connection");
            Platform.exit();
        }
    }

    private void initializeGame(String response) {
        // black can choose the board, if we got a T it means we can choose the board
        Player opponent = new Player(response.substring(4), null);
        if (response.substring(2, 3).equals("T")) {
            player.setColor(Player.Color.BLACK);
            opponent.setColor(Player.Color.WHITE);
        } else {
            player.setColor(Player.Color.WHITE);
            opponent.setColor(Player.Color.BLACK);
        }
        Platform.runLater(() -> listener.gameEstablished(opponent));
        playerRefresher.stop();
        state = WaitingState.IDLE;
    }

    /**
     * Shuts down the game and the connection.
     * Used when the server or opponent sent a malicious response.
     */
    public void detectException() {
        System.err.println("Detected malicious response from server or opponent");
        System.err.println("Shutting down game and connection :/");
        try {
            stop();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Platform.exit();
        }
    }

    public void stop() throws IOException {
        try {
            out.println("Q");
        } finally {
            socket.close();
        }
    }

    private enum WaitingState {
        IDLE,
        IDENTIFY,
        PLAYERLIST,
        DEQUEUED,
        BOARD,
        OPPONENT,
        GAME
    }
}