package mijnlieff.client.game;

import javafx.scene.Scene;
import mijnlieff.client.board.Board;
import mijnlieff.client.board.Deck;
import mijnlieff.client.board.DeckPane;
import mijnlieff.client.board.BoardSetting;

public class GameCompanion {

    public Board model;

    public void setBoardSetting(BoardSetting boardSetting) {
        model.setBoardSetting(boardSetting);
        model.resetCurrentMove();
    }

    public void setScene(Scene scene) {
        initializeDecks(scene, model);
    }

    public static void initializeDecks(Scene scene, Board model) {
        Deck whiteDeck = new Deck(Player.Color.WHITE, model);
        Deck blackDeck = new Deck(Player.Color.BLACK, model);
        ((DeckPane)scene.lookup("#white-deck")).setModel(whiteDeck);
        ((DeckPane)scene.lookup("#black-deck")).setModel(blackDeck);
        whiteDeck.forceUpdate();
        blackDeck.forceUpdate();
    }
}
