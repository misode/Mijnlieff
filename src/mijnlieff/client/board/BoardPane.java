package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import mijnlieff.client.game.GameCompanion;

/**
 * View of the board
 */
public class BoardPane extends GridPane implements InvalidationListener {

    private Board model;
    private GameCompanion controller;

    public BoardPane(Board model, GameCompanion controller) {
        this.model = model;
        this.controller = controller;
        model.addListener(this);

        getStyleClass().add("board-pane");
        setAlignment(Pos.CENTER);
    }

    private void initialize() {
        int size = 500 / Math.max(model.getWidth(), model.getHeight());
        System.out.println("Size: " + size);
        for (int i = 0; i < model.getWidth(); i++) {
            for (int j = 0; j < model.getHeight(); j++) {
                if (model.hasCell(i, j)) {
                    TilePane tilePane = new TilePane(null, controller::selectBoardTile);
                    tilePane.setPrefSize(size, size);
                    add(tilePane, i, j);
                } else {
                    Rectangle rectangle = new Rectangle(size, size);
                    rectangle.getStyleClass().add("board-empty");
                    add(rectangle, i, j);
                }
            }
        }
    }

    @Override
    public void invalidated(Observable observable) {
        if (getChildren().size() == 0) {
            initialize();
        }
        for (Node n : getChildren()) {
            int x = GridPane.getColumnIndex(n);
            int y = GridPane.getRowIndex(n);
            if (model.hasCell(x, y)) {
                Tile tile = model.getTile(x, y);
                TilePane cell = (TilePane) n;
                cell.setTile(tile);
                cell.setValid(model.isValidCell(x, y));
                cell.setClickeable(model.isValidCell(x, y) && model.isOnTurn());
            }
        }
    }
}
