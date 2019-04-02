package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import mijnlieff.client.game.GameCompanion;

/**
 * View of the board
 */

public class BoardPane extends GridPane implements InvalidationListener {

    private static Image emptyCell = new Image("mijnlieff/client/img/empty.png");

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
        for (int i = 0; i < model.getWidth(); i++) {
            for (int j = 0; j < model.getHeight(); j++) {
                if (model.hasCell(i, j)) {
                    ImageView image = new ImageView(emptyCell);
                    image.getStyleClass().add("board-cell");
                    image.setFitHeight(100);
                    image.setFitWidth(100);
                    add(image, i, j);
                } else {
                    Rectangle rectangle = new Rectangle(100, 100);
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
                ImageView cell = (ImageView) n;
                if (tile == null) {
                    cell.setImage(emptyCell);
                    if (controller != null) {
                        cell.setOnMouseClicked(controller::selectBoardTile);
                    }
                } else {
                    cell.setImage(tile.getImage());
                }
            }
        }
    }
}
