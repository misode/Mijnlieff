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

    private Connection connection;

    private ArrayList<Move> moves;
    private int width, height;
    private boolean[][] cells;
    private int currentMove;
    private boolean reachedEnd;

    public Board() {
        moves = new ArrayList<>();
        width = 0;
        height = 0;
        currentMove = -1;
        reachedEnd = false;
    }

    public void setBoardSetting(BoardSetting boardSetting) {
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

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void receivedMove(String response) {
        Move move = decodeMove(connection.getPlayer().getColor(), response);
        moves.add(move);
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

    public void sendMove(Move move) {
        connection.sendMove(encodeMove(move));
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
        Move lastMove = moves.get(moves.size() - 1);
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(hasCell(i, j) && getTile(i, j) == null &&
                        lastMove.getTile().getType().isAllowed(i - lastMove.getX(), j - lastMove.getY())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean setCurrentMove(int newMove) {
        if(currentMove == newMove) return false;
        if(newMove > moves.size()) {
            if(reachedEnd) return false;
            boolean success = requestMove();
            if(!success) return false;
            currentMove = newMove;
        } else {
            currentMove = newMove;
            if(currentMove < 0) {
                currentMove = 0;
            }
        }
        fireInvalidationEvent();
        return true;
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
