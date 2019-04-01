package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
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
        for(Tile t : model.getTiles()) {
            ImageView image = new ImageView(t.getImage());
            if(controller != null) {
                image.setOnMouseClicked(controller::selectDeckTile);
            }
            image.getStyleClass().add("deck-image");
            image.setFitWidth(75);
            image.setFitHeight(75);
            getChildren().add(image);
        }
    }
}
