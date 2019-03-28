package mijnlieff.client;

import javafx.beans.Observable;
import javafx.concurrent.Worker;
import mijnlieff.client.board.Move;
import mijnlieff.client.board.Tile;
import mijnlieff.client.establisher.board.BoardEstablisher;
import mijnlieff.client.establisher.board.BoardEstablisherTask;
import mijnlieff.client.establisher.board.BoardSetting;
import mijnlieff.client.establisher.game.GameEstablisher;
import mijnlieff.client.establisher.game.JoinTask;

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

    private JoinTask joinTask;
    private GameEstablisher gameEstablisher;

    private BoardEstablisherTask boardTask;
    private BoardEstablisher boardEstablisher;

    // TODO: remove temporary fields
    private String playerName;
    private boolean playerJoined;

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

        playerJoined = false;
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

            // TODO: remove temporary response
            response = "+";
            playerName = username;

            return response.equals("+");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Requests the player list from the server.
     * @return a list of player names in an {@link ArrayList}. Can be empty.
     */
    public ArrayList<String> getPlayerList() {
        out.println("W");
        ArrayList<String> playerNames = new ArrayList<>();
        try {
            String response = in.readLine();
            while(response.startsWith("+") && response.length() > 2) {
                playerNames.add(response.substring(2));
                response = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: remove temporary names
        playerNames.add("Alice");
        playerNames.add("Bob");
        playerNames.add("Carl");
        playerNames.add("Dave");
        if(playerJoined) {
            playerNames.add(playerName);
        }
        return playerNames;
    }

    /**
     * Marks the player on the global player list, available for any opponent to start a game.
     * Also starts a {@link JoinTask} to wait for an opponent.
     * @param gameEstablisher the {@link GameEstablisher} to call when an opponent has been found
     */
    public void enqueue(GameEstablisher gameEstablisher) {
        this.gameEstablisher = gameEstablisher;
        out.println("P");

        // TODO: remove temporary statement
        playerJoined = true;

        joinTask = new JoinTask(in);
        joinTask.stateProperty().addListener(o -> this.foundOpponent());
        new Thread(joinTask).start();
    }

    /**
     * Notifies the {@link GameEstablisher} if it found an opponent.
     * Called by {@link JoinTask}
     * @see JoinTask.Opponent
     */
    private void foundOpponent() {
        if(joinTask.getState() == Worker.State.SUCCEEDED) {
            gameEstablisher.joined(joinTask.getValue());
        } else if(joinTask.getState() == Worker.State.FAILED) {
            System.err.println("Failed joining game");
        }
    }

    /**
     * Removes the player from the global player list.
     * @return {@code true} if the player was successfully removed from the queue
     *         {@code false} if something went wrong
     */
    public boolean dequeue() {
        out.println("R");
        try {
            String response = in.readLine();

            // TODO: remove temporary response
            response = "+";
            playerJoined = response.equals("+");

            return response.equals("+");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param opponentName the name of the opponent that is used to start the game.
     * @return {@link Tile.Player} the color of the opponent decided by the server
     * @see Tile.Player
     */
    public Tile.Player selectOpponent(String opponentName) {
        out.println("C "+opponentName);
        try {
            String response = in.readLine();

            // TODO: remove temporary response
            response = "T";

            if(response.equals("T")) {
                return Tile.Player.WHITE;
            } else if(response.equals("F")) {
                return Tile.Player.BLACK;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Starts waiting for the opponent to choose a board setting.
     * @param boardEstablisher the {@link BoardEstablisher} to call when a board setting has been chosen by the opponent
     * @see BoardSetting
     * @see JoinTask.Opponent
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