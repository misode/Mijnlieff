package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deck model, also listens to the board model
 */

public class Deck implements Observable, InvalidationListener {

    private Board board;
    private Tile.Player player;
    private ArrayList<Tile> tiles;

    public Deck(Tile.Player player, Board board) {
        this.player = player;
        this.board = board;
        board.addListener(this);

        this.player = player;
        fillDeck();
    }

    private void fillDeck() {
        tiles = new ArrayList<>();
        for(Tile.Type t : Tile.Type.values()) {
            for(int i = 0; i < 2; i++) {
                tiles.add(new Tile(player, t));
            }
        }
    }

    private void removeOneFromDeck(Tile t) {
        for(int i = tiles.size() - 1; i >= 0; i--) {
            if(t.equals(tiles.get(i))) {
                tiles.remove(i);
                return;
            }
        }
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }

    public void forceUpdate() {
        fireInvalidationEvent();
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
        for(Move m : board.getMoves()) {
            removeOneFromDeck(m.getTile());
        }
        fireInvalidationEvent();
    }

    public String toString() {
        return "[" + tiles.stream().map(Tile::toString).collect(Collectors.joining(", ")) + "]";
    }
}
