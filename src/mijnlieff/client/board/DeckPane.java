package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class DeckPane extends VBox implements InvalidationListener {

    private Deck model;

    public DeckPane() {
        getStyleClass().add("deck-pane");
        setAlignment(Pos.CENTER);
    }

    public void setModel(Deck model) {
        this.model = model;
        model.addListener(this);
    }

    public Deck getModel() {
        return model;
    }

    @Override
    public void invalidated(Observable observable) {
        getChildren().clear();
        for(Tile t : model.getTiles()) {
            ImageView image = new ImageView(t.getImage());
            image.getStyleClass().add("deck-image");
            image.setFitWidth(75);
            image.setFitHeight(75);
            getChildren().add(image);
        }
    }
}
