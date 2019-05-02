package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import mijnlieff.client.game.GameCompanion;

public class DeckPane extends VBox implements InvalidationListener {

    private Deck model;
    private GameCompanion controller;

    public DeckPane(Deck model, GameCompanion controller) {
        this.model = model;
        this.controller = controller;
        model.addListener(this);

        getStyleClass().add("deck-pane");
        setAlignment(Pos.CENTER);
    }

    @Override
    public void invalidated(Observable observable) {
        getChildren().clear();
        for (int i = 0; i < model.getTiles().size(); i += 1) {
            Tile t = model.getTiles().get(i);
            TilePane tilePane = new TilePane(t, e ->
                    controller.selectDeckTile(model.getPlayerColor(), getChildrenUnmodifiable().indexOf((TilePane)e.getSource())));
            tilePane.setSelected(model.getSelectedTile() == i);
            tilePane.setClickeable(model.getBoard().isOnTurn() && model.getPlayerColor() == model.getBoard().getPlayer().getColor());
            getChildren().add(tilePane);
        }
    }
}
