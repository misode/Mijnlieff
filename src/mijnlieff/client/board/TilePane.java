package mijnlieff.client.board;

import javafx.scene.layout.Pane;

public class TilePane extends Pane {

    public TilePane(double width, double height) {
        setPrefSize(width, height);
    }

    public void setTile(Tile tile) {
        getStyleClass().clear();
        if (tile == null) {
            getStyleClass().add("empty");
        } else {
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
}
