package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import mijnlieff.client.game.GameCompanion;

public class DeckPane extends VBox implements InvalidationListener {

    private Deck deck;
    private GameCompanion controller;

    public DeckPane(Deck deck, GameCompanion controller) {
        this.deck = deck;
        this.controller = controller;
        deck.addListener(this);

        setAlignment(Pos.CENTER);
    }

    @Override
    public void invalidated(Observable observable) {
        getChildren().clear();
        for (int i = 0; i < deck.getTiles().size(); i += 1) {
            TilePane tilePane = new TilePane(75, 75);
            tilePane.setTile(deck.getTiles().get(i));
            tilePane.setOnMouseClicked(e ->
                    controller.selectDeckTile(deck, getChildrenUnmodifiable().indexOf((TilePane)e.getSource())));
            tilePane.setSelected(deck.getSelectedTile() == i);
            tilePane.setClickeable(deck.getBoard().isOnTurn() && deck.getPlayerColor() == deck.getBoard().getPlayer().getColor());
            getChildren().add(tilePane);
        }
    }
}
