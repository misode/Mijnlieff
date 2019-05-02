package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import mijnlieff.client.game.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deck model, also listens to the board model
 */
public class Deck implements Observable, InvalidationListener {

    private Board board;
    private Player.Color player;
    private ArrayList<Tile> tiles;
    private int selectedTile;

    public Deck(Player.Color player, Board board) {
        this.player = player;
        this.board = board;
        board.addListener(this);
        fillDeck();
        selectedTile = -1;
    }

    public Board getBoard() {
        return board;
    }
    
    public Player.Color getPlayerColor() {
        return player;
    }

    private void fillDeck() {
        tiles = new ArrayList<>();
        for (Tile.Type t : Tile.Type.values()) {
            for (int i = 0; i < 2; i++) {
                tiles.add(new Tile(player, t));
            }
        }
    }

    public void removeOneFromDeck(Tile t) {
        for (int i = tiles.size() - 1; i >= 0; i--) {
            if (t.equals(tiles.get(i))) {
                if (i == selectedTile) {
                    setSelectedTile(-1);
                }
                tiles.remove(i);
                return;
            }
        }
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }

    public void setSelectedTile(int row) {
        if(selectedTile == row) return;
        selectedTile = row;
        fireInvalidationEvent();
    }

    public int getSelectedTile() {
        return selectedTile;
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

    @Override
    public void invalidated(Observable observable) {
        fillDeck();
        for (Move m : board.getMoves()) {
            removeOneFromDeck(m.getTile());
        }
        fireInvalidationEvent();
    }

    public String toString() {
        return "[" + tiles.stream().map(Tile::toString).collect(Collectors.joining(", ")) + "]";
    }
}
