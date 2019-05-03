package mijnlieff.client;

import javafx.application.Platform;
import mijnlieff.client.board.*;
import mijnlieff.client.game.Player;

import java.io.IOException;
import java.util.Random;

public class Bot extends ConnectionListener{

    private String hostName;
    private int portNumber;

    private Board board;
    private Deck playerDeck;
    private Random RG;
    private boolean skippedMove;

    public Bot(String hostName, int portNumber) {
        super(new Connection());
        this.RG = new Random();
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.skippedMove = false;
        initialize();
    }

    private void initialize() {
        System.err.println("Opening a new connection...");
        try {
            connection.start(hostName, portNumber);
            int random = RG.nextInt(1000000);
            connection.identify("Bot" + random);
            connection.enqueue();
            connection.setListener(this);
        } catch (IOException e) {
            System.err.println("Failed to make a connection to " + hostName + ":" + portNumber);
            e.printStackTrace();
            Platform.exit();
        }
    }

    public void gameEstablished(Player opponent) {
        System.out.println("Established game with player " + opponent.getUsername());
        if (opponent.getColor() == Player.Color.WHITE) {
            connection.send(BoardSetting.DEFAULT.toString());
            board = new Board(connection, BoardSetting.DEFAULT);
            connection.setListener(this);
            playerDeck = board.getDeck(connection.getPlayer().getColor());
        }
    }

    public void received(String response) {
        if (board == null) {
            board = new Board(connection, new BoardSetting(response));
            board.setOnTurn(true);
            connection.setListener(this);
            playerDeck = board.getDeck(connection.getPlayer().getColor());
        } else {
            board.setOnTurn(true);
            if (response != null) {
                Move move = Move.decode(connection.getPlayer().getColor().next(), response);
                System.out.println("> " + move);
                board.addMove(move);
                board.calculateValidCells();
                if (board.wasBlocked()) {
                    connection.send(null);
                    skippedMove = true;
                    board.setOnTurn(false);
                }
            } else {
                if (skippedMove || playerDeck.getTiles().size() == 0) {
                    reset();
                }
            }
        }
        if (board.isOnTurn()) {
            if (board.wasBlocked() || playerDeck.getTiles().size() == 0) {
                connection.send(null);
                skippedMove = true;
                if (board.getDeck(connection.getOpponent().getColor()).getTiles().size() == 0) {
                    reset();
                }
            } else {
                Move randomMove = randomMove();
                System.out.println("< " + randomMove);
                board.addMove(randomMove);
                board.calculateValidCells();
                connection.send(randomMove.encode());
            }
        }
    }

    private Move randomMove() {
        Tile deckTile = playerDeck.getTiles().get(RG.nextInt(playerDeck.getTiles().size()));
        Move attempt = new Move(RG.nextInt(board.getWidth()), RG.nextInt(board.getHeight()), deckTile);

        while (!board.isValid(attempt.getX(), attempt.getY()) || board.getTile(attempt.getX(), attempt.getY()) != null) {
            attempt.setX(RG.nextInt(board.getWidth()));
            attempt.setY(RG.nextInt(board.getHeight()));
        }
        return attempt;
    }

    private void reset() {
        try {
            connection.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection = new Connection();
        initialize();
    }
}
