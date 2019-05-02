package mijnlieff.client.board;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class TilePane extends Pane {

    private Tile tile;

    public TilePane(Tile tile, EventHandler<MouseEvent> mouseEventHandler) {
        setTile(tile);
        setOnMouseClicked(mouseEventHandler);
        getStyleClass().add("cell");
    }

    public void setTile(Tile tile) {
        this.tile = tile;
        if(tile == null) {
            getStyleClass().setAll("cell", "empty");
        } else {
            getStyleClass().remove("empty");
            getStyleClass().add(tile.getType().getName());
            getStyleClass().add(tile.getPlayer().getName());
        }
    }

    public void setSelected(boolean selected) {
        if (selected) {
            getStyleClass().add("selected");
        } else {
            getStyleClass().remove("selected");
        }
    }

    public void setValid(boolean valid) {
        if (valid) {
            getStyleClass().add("valid");
        } else {
            getStyleClass().remove("valid");
        }
    }

    public void setClickeable(boolean clickeable) {
        if (clickeable) {
            getStyleClass().add("clickeable");
        } else {
            getStyleClass().remove("clickeable");
        }
    }

    public Tile getTile() {
        return tile;
    }
}
