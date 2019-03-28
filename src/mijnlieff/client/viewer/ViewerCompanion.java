package mijnlieff.client.viewer;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import mijnlieff.client.Connection;
import mijnlieff.client.board.Board;
import mijnlieff.client.board.Deck;
import mijnlieff.client.board.DeckPane;
import mijnlieff.client.board.Tile;

public class ViewerCompanion {

    public Board model;

    public TextField serverHost;

    public void initialize() {
        model.resetCurrentMove();
    }

    public Board getModel() {
        return model;
    }

    public void setConnection(Connection connection) {
        model.setConnection(connection);
    }

    public void setScene(Scene scene) {
        Deck whiteDeck = new Deck(Tile.Player.WHITE, model);
        Deck blackDeck = new Deck(Tile.Player.BLACK, model);
        ((DeckPane)scene.lookup("#white-deck")).setModel(whiteDeck);
        ((DeckPane)scene.lookup("#black-deck")).setModel(blackDeck);
        whiteDeck.forceUpdate();
        blackDeck.forceUpdate();
    }
}
