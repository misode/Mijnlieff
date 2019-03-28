package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import mijnlieff.client.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model of the board
 */

public class Board implements Observable {

    private Connection connection;

    private ArrayList<Move> moves;
    private int width, height;
    private int currentMove;
    private boolean reachedEnd;

    public Board() {
        moves = new ArrayList<>();
        this.width = 4;
        this.height = 4;
        currentMove = -1;
        reachedEnd = false;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private void requestMove() {
        Tile.Player player = Tile.Player.WHITE;
        if(moves.size() > 0) {
            player = moves.get(moves.size() - 1).getTile().getPlayer();
            if(!wasBlockingMove()) {
                player = player.next();
            }
        }
        try {
            String[] msg = connection.next().split(" ");
            if(msg[1].equals("T")) reachedEnd = true;
            int moveX = Integer.parseInt(msg[2]);
            int moveY = Integer.parseInt(msg[3]);
            Tile newTile = new Tile(player, Tile.Type.fromChar(msg[4]));
            moves.add(new Move(moveX, moveY, newTile));
        } catch (IOException e1) {
            System.err.println("Fout bij ophalen van volgende zet");
        }
    }

    public boolean wasBlockingMove() {
        Move lastMove = moves.get(moves.size() - 1);
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(!hasTile(i, j) && lastMove.getTile().getType().isAllowed(i - lastMove.getX(), j - lastMove.getY())) {
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
            currentMove = newMove;
            requestMove();
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

    public boolean hasTile(int x, int y) {
        for(Move m : moves) {
            if(m.getX() == x && m.getY() == y) {
                return true;
            }
        }
        return false;
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
