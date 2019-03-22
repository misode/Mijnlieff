package mijnlieff.client;

import javafx.scene.Scene;
import mijnlieff.client.board.Board;
import mijnlieff.client.board.Deck;
import mijnlieff.client.board.DeckPane;
import mijnlieff.client.board.Tile;

public class Companion {

    public Board model;

    private Scene scene;

    public void initialize() {
        model.resetCurrentMove();
    }

    public Board getModel() {
        return model;
    }

    public void setEchoClient(EchoClient echoClient) {
        model.setEchoClient(echoClient);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        Deck whiteDeck = new Deck(Tile.Player.WHITE, model);
        Deck blackDeck = new Deck(Tile.Player.BLACK, model);
        ((DeckPane)scene.lookup("#white-deck")).setModel(whiteDeck);
        ((DeckPane)scene.lookup("#black-deck")).setModel(blackDeck);
        whiteDeck.forceUpdate();
        blackDeck.forceUpdate();
    }
}
