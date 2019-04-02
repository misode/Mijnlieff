package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import mijnlieff.client.Connection;
import mijnlieff.client.ConnectionListener;
import mijnlieff.client.game.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model of the board
 */

public class Board extends ConnectionListener implements Observable {

    private ArrayList<Move> moves;
    private int width, height;
    private boolean[][] cells;
    private int currentMove;
    private boolean reachedEnd;
    private boolean onTurn;

    private Deck whiteDeck;
    private Deck blackDeck;

    public Board(Connection connection, BoardSetting boardSetting) {
        super(connection);

        moves = new ArrayList<>();
        width = 0;
        height = 0;
        currentMove = -1;
        reachedEnd = false;
        onTurn = false;
        whiteDeck = new Deck(Player.Color.WHITE, this);
        blackDeck = new Deck(Player.Color.BLACK, this);

        // In the viewer mode the player's color will be null meaning onTurn will stay false
        onTurn = connection.getPlayer().getColor() == Player.Color.WHITE;

        for(int i = 0; i < 4; i++) {
            if(boardSetting.getX(i) + 2 > width) width = boardSetting.getX(i) + 2;
            if(boardSetting.getY(i) + 2 > height) height = boardSetting.getY(i) + 2;
        }
        cells = new boolean[width][height];
        for(int i = 0; i < 4; i++) {
            int x = boardSetting.getX(i);
            int y = boardSetting.getY(i);
            cells[x][y] = true;
            cells[x+1][y] = true;
            cells[x][y+1] = true;
            cells[x+1][y+1] = true;
        }
    }

    public Deck getDeck(Player.Color playerColor) {
        if(playerColor == Player.Color.WHITE) return whiteDeck;
        else return blackDeck;
    }

    public Player getPlayer() {
        return connection.getPlayer();
    }

    public void receivedMove(String response) {
        System.out.println("Recieved response!");
        Move move = decodeMove(connection.getPlayer().getColor().next(), response);
        moves.add(move);
        onTurn = true;
        currentMove += 1;
        System.out.println("Added move " + move);
        fireInvalidationEvent();
    }

    private boolean requestMove() {
        Player.Color player = Player.Color.WHITE;
        if(moves.size() > 0) {
            player = moves.get(moves.size() - 1).getTile().getPlayer();
            if(!wasBlockingMove()) {
                player = player.next();
            }
        }
        try {
            String response = connection.viewNext();
            Move newMove = decodeMove(player, response);
            if(newMove == null) return false;
            moves.add(newMove);
            return true;
        } catch (IOException e) {
            System.err.println("Fout bij ophalen van volgende zet");
            return false;
        }
    }

    public void transferTile(Tile deckTile, int x, int y) {
        System.out.println("Valid cell? " + isValidCell(x, y));
        if (!isValidCell(x, y)) return;
        addMove(new Move(x, y, deckTile));
        getDeck(connection.getPlayer().getColor()).removeOneFromDeck(deckTile);
        fireInvalidationEvent();
    }

    private void addMove(Move move) {
        System.out.println("Adding move " + move);
        moves.add(move);
        connection.sendMove(encodeMove(move));
        onTurn = false;
        currentMove += 1;
    }

    private Move decodeMove(Player.Color player, String response) {
        String[] msg = response.split(" ");
        if(msg[1].equals("T")) {
            reachedEnd = true;
        }
        // converting rows/columns to x/y
        int moveY = Integer.parseInt(msg[2]);
        int moveX = Integer.parseInt(msg[3]);
        Tile.Type type = Tile.Type.fromChar(msg[4]);
        if(type == null) {
            connection.detectException();
            return null;
        }
        Tile newTile = new Tile(player, type);
        return new Move(moveX, moveY, newTile);
    }

    private String encodeMove(Move move) {
        String message = "X F";
        message += " " + move.getY();
        message += " " + move.getX();
        message += " " + move.getTile().getType().getChar();
        return message;
    }

    private boolean wasBlockingMove() {
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(!isValidCell(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValidCell(int x, int y) {
        if(moves.size() > 0) {
            Move lastMove = moves.get(moves.size() - 1);
            if(!lastMove.getTile().getType().isAllowed(x - lastMove.getX(), y - lastMove.getY())) {
                return false;
            }
        }
        return hasCell(x, y) && getTile(x, y) == null;
    }

    public boolean isOnTurn() {
        return onTurn;
    }

    public void setCurrentMove(int newMove) {
        if(currentMove == newMove) return;
        if(newMove > moves.size()) {
            if(reachedEnd) return;
            boolean success = requestMove();
            if(!success) return;
            currentMove = newMove;
        } else {
            currentMove = newMove;
            if(currentMove < 0) {
                currentMove = 0;
            }
        }
        fireInvalidationEvent();
    }

    public int getCurrentMove() {
        return currentMove;
    }

    public boolean hasNextMove() {
        return !(reachedEnd && getCurrentMove() >= moves.size());
    }

    public List<Move> getMoves() {
        return moves.subList(0, currentMove);
    }

    public void resetCurrentMove() {
        if(currentMove == 0) return;
        currentMove = 0;
        fireInvalidationEvent();
    }

    public boolean hasCell(int x, int y) {
        if(x < 0 || y < 0 || x >= width || y >= height) return false;
        return cells[x][y];
    }

    public Tile getTile(int x, int y) {
        for(Move m : getMoves()) {
            if(m.getX() == x && m.getY() == y) {
                return m.getTile();
            }
        }
        return null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private List<InvalidationListener> listenerList = new ArrayList<>();

    private void fireInvalidationEvent () {
        for (InvalidationListener listener : listenerList) {
            listener.invalidated(this);
        }
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.listenerList.add(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.listenerList.remove(invalidationListener);
    }

    public String toString() {
        return "[" + moves.stream().limit(currentMove).map(Move::toString).collect(Collectors.joining(", ")) + "]";
    }
}
