package mijnlieff.client;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import mijnlieff.client.board.Move;
import mijnlieff.client.board.Tile;
import mijnlieff.client.establisher.board.BoardEstablisher;
import mijnlieff.client.establisher.board.BoardEstablisherTask;
import mijnlieff.client.establisher.board.BoardSetting;
import mijnlieff.client.establisher.game.GameEstablisher;
import mijnlieff.client.establisher.game.Opponent;

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

    private BoardEstablisherTask boardTask;
    private BoardEstablisher boardEstablisher;
    private String opponentName;
    private boolean requestedPlayerNames;

    /**
     * Creates a new {@link Socket} with the specified host and port
     * @param hostName the host name to create this connection
     * @param portNumber the port number to create this connection
     * @throws IOException if the socket could not be created
     * @see Socket
     */
    public Connection(String hostName, int portNumber) throws IOException {
        System.out.println("Opening connection: " + hostName + " " + portNumber);
        socket = new Socket(hostName, portNumber);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        requestedPlayerNames = false;
    }

    /**
     * Lets the server know who the player is.
     * No authentication is required.
     * @param username the username to pass to the server
     * @return {@code true} if the username is valid
     *         {@code false} if the username is invalid because it was already taken
     */
    public boolean identify(String username) {
        out.println("I " + username);
        try {
            String response = in.readLine();
            System.out.println(response);
            return response.equals("+");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Requests the player list from the server.
     */
    public void requestPlayerList() {
        out.println("W");
        requestedPlayerNames = true;
    }

    /**
     * Marks the player on the global player list, available for any opponent to start a game.
     */
    public void enqueue() {
        out.println("P");
    }

    /**
     * Removes the player from the global player list.
     */
    public void dequeue() {
        out.println("R");
    }

    /**
     *
     * @param opponentName the name of the opponent that is used to start the game.
     */
    public void selectOpponent(String opponentName) {
        this.opponentName = opponentName;
        out.println("C "+opponentName);
    }

    public void watchGameEstablishing(GameEstablisher gameEstablisher) {
        boolean gameEstablished = false;
        try {
            String response;
            while (!gameEstablished && (response = in.readLine()) != null) {
                if(requestedPlayerNames && response.equals("+")) {
                    refreshedPlayerList(gameEstablisher, response);
                } else if(response.equals("T") || response.equals("F")) {
                    Tile.Player player = Tile.Player.WHITE;
                    if (response.equals("T")) player = Tile.Player.BLACK;
                    Opponent opponent = new Opponent(player, opponentName);
                    Platform.runLater(() -> gameEstablisher.joined(opponent));
                    gameEstablished = true;
                } else if(response.length() > 4) {
                    if(response.substring(3, 4).equals(" ")) {
                        Tile.Player player = Tile.Player.BLACK;
                        if (response.charAt(2) == 'T') player = Tile.Player.WHITE;
                        Opponent opponent = new Opponent(player, response.substring(4));
                        Platform.runLater(() -> gameEstablisher.joined(opponent));
                        gameEstablished = true;
                    } else {
                        refreshedPlayerList(gameEstablisher, response);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Closed connection");
        }
    }

    private void refreshedPlayerList(GameEstablisher establisher, String response) {
        ArrayList<String> playerNames = new ArrayList<>();
        try {
            while (response.startsWith("+") && response.length() > 2) {
                playerNames.add(response.substring(2));
                response = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> establisher.refreshedPlayerList(playerNames));
        requestedPlayerNames = false;
    }

    /**
     * Starts waiting for the opponent to choose a board setting.
     * @param boardEstablisher the {@link BoardEstablisher} to call when a board setting has been chosen by the opponent
     * @see BoardSetting
     * @see Opponent
     */
    public void waitBoard(BoardEstablisher boardEstablisher) {
        this.boardEstablisher = boardEstablisher;

        boardTask = new BoardEstablisherTask(in);
        boardTask.stateProperty().addListener(o -> this.recievedBoard());
        new Thread(boardTask).start();
    }

    /**
     * Notifies the {@link BoardEstablisher} if the board has been chosen.
     * Called by {@link BoardEstablisherTask}
     */
    private void recievedBoard() {
        if(boardTask.getState() == Worker.State.SUCCEEDED) {
            boardEstablisher.selectedBoard(boardTask.getValue());
        } else if(boardTask.getState() == Worker.State.FAILED) {
            System.err.println("Failed getting a board setting");
        }
    }

    public void sendBoard(BoardSetting boardSetting) {
        out.println("X " + boardSetting.toString());
    }

    public void sendMove(Move move) {
        // TODO: implement method
    }

    public String viewNext() throws IOException {
        out.println("X");
        return in.readLine();
    }

    public void stop() throws IOException {
        try {
            out.println("Q");
        } finally {
            socket.close();
            out.close();
            in.close();
        }
    }
}