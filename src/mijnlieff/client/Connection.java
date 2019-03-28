package mijnlieff.client;

import javafx.beans.Observable;
import javafx.concurrent.Worker;
import mijnlieff.client.board.Tile;

import javax.security.auth.callback.Callback;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Connection{

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JoinTask joinTask;
    private GameEstablisher gameEstablisher;

    private BoardEstablisherTask boardTask;
    private BoardEstablisher boardEstablisher;

    // TODO: remove temporary field
    private String playerName;
    private boolean playerJoined;

    public Connection(String hostName, int portNumber) throws IOException {
        System.out.println("Opening connection: " + hostName + " " + portNumber);
        socket = new Socket(hostName, portNumber);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        playerJoined = false;
    }

    public boolean identify(String username) throws IOException {
        out.println("I " + username);
        playerName = username;
        String response = in.readLine();
        // TODO: remove temporary response
        response = "+";
        return response.equals("+");
    }

    public ArrayList<String> getPlayerList() throws IOException {
        out.println("W");
        ArrayList<String> playerNames = new ArrayList<>();
        //String response = in.readLine();
        //while(response.startsWith("+") && response.length() > 2) {
        //    playerNames.add(response.substring(2));
        //    response = in.readLine();
        //}
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

    public void enqueue(GameEstablisher gameEstablisher) throws IOException {
        this.gameEstablisher = gameEstablisher;
        out.println("P");

        playerJoined = true;

        joinTask = new JoinTask(in);
        joinTask.stateProperty().addListener(this::joined);
        new Thread(joinTask).start();
    }

    private void joined(Observable observable) {
        if(joinTask.getState() == Worker.State.SUCCEEDED) {
            gameEstablisher.joined(joinTask.getValue());
        } else if(joinTask.getState() == Worker.State.FAILED) {
            System.err.println("Failed joining game");
        }
    }

    public boolean dequeue() throws IOException {
        out.println("R");
        playerJoined = false;
        //String response = in.readLine();
        // TODO: remove temporary response
        String response = "+";
        return response.equals("+");
    }

    public Tile.Player selectOpponent(String opponentName) throws IOException {
        out.println("C "+opponentName);
        //String response = in.readLine();
        // TODO: remove temporary response
        String response = "T";
        if(response.equals("T")) {
            return Tile.Player.WHITE;
        } else if(response.equals("F")) {
            return Tile.Player.BLACK;
        }
        return null;
    }

    public void waitBoard(BoardEstablisher boardEstablisher) {
        this.boardEstablisher = boardEstablisher;

        boardTask = new BoardEstablisherTask(in);
        boardTask.stateProperty().addListener(this::selectedBoard);
        new Thread(boardTask).start();
    }

    private void selectedBoard(Observable observable) {
        if(boardTask.getState() == Worker.State.SUCCEEDED) {
            boardEstablisher.selectedBoard(boardTask.getValue());
        } else if(boardTask.getState() == Worker.State.FAILED) {
            System.err.println("Failed joining game");
        }
    }

    public void decideBoard(String boardSetting) {
        out.println("X " + boardSetting);
    }

    public String next() throws IOException {
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