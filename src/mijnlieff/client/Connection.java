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
    private Player opponent;
    private boolean enqueued;

    private Timeline refresher;
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
        opponent = new Player("Opponent", null);
        enqueued = false;
    }

    private void startRefreshing() {
        refresher = new Timeline(new KeyFrame(Duration.seconds(1), e -> refresh()));
        refresher.setCycleCount(Timeline.INDEFINITE);
        refresher.play();
    }

    public Player getPlayer() {
        return player;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setListener(ConnectionListener listener) {
        this.listener = listener;
    }

    /**
     * Lets the server know who the player is.
     * @param username the username to pass to the server
     */
    public void identify(String username) {
        out.println("I " + username);
        try {
            String response = in.readLine();
            boolean success = response.equals("+");
            if (success) {
                player.setUsername(username);
                startRefreshing();
            }
            Platform.runLater(() -> listener.identified(success));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Requests the player list from the server and also checks if a game has been made.
     */
    private void refresh() {
        out.println("W");
        try {
            ArrayList<String> playerNames = new ArrayList<>();
            String response = in.readLine();
            while (!response.equals("+")) {
                if (response.length() >= 4 && response.substring(3, 4).equals(" ")) {
                    initializeGame(response);
                } else {
                    playerNames.add(response.substring(2));
                }
                response = in.readLine();
            }
            listener.updatePlayerList(playerNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Marks the player on the global player list, available for any opponent to start a game.
     */
    public void enqueue() {
        out.println("P");
        enqueued = true;
    }

    /**
     * Removes the player from the global player list.
     */
    public void dequeue() {
        out.println("R");
        try {
            String response = in.readLine();
            enqueued = !response.equals("+");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Selects the opponent from the queued player list to play against.
     * @param opponentName the name of the opponent that is used to start the game.
     */
    public void selectOpponent(String opponentName) {
        // Make sure the player is dequeued before we try to select an opponent
        if (enqueued) dequeue();

        out.println("C "+opponentName);
        try {
            String response = in.readLine();
            if(response.length() >= 4 && response.substring(3, 4).equals(" ")) {
                initializeGame(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void send(String message) {
        if (message == null) {
            out.println("X");
        } else {
            out.println("X " + message);
        }
    }

    public String read() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Listens to the server for responses during the game.
     */
    private void listen() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                if (response.equals("Q")) {
                    System.err.println("Server or opponent decided to close the connection...");
                    Platform.exit();
                } else {
                    String finalResponse = (response.length() < 3) ? null : response.substring(2);
                    Platform.runLater(() -> listener.received(finalResponse));
                }
            }
        } catch (IOException e) {
            System.err.println("Closed connection");
            Platform.exit();
        }
    }

    private void initializeGame(String response) {
        // black can choose the board, if we got a T it means we can choose the board
        opponent = new Player(response.substring(4), null);
        if (response.substring(2, 3).equals("T")) {
            player.setColor(Player.Color.BLACK);
            opponent.setColor(Player.Color.WHITE);
        } else {
            player.setColor(Player.Color.WHITE);
            opponent.setColor(Player.Color.BLACK);
        }
        listener.gameEstablished(opponent);
        refresher.stop();
        new Thread(this::listen).start();
    }

    public void stop() throws IOException {
        try {
            out.println("Q");
        } finally {
            socket.close();
        }
    }
}