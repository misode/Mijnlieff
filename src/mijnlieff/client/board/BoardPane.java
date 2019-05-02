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

    private Board board;
    private GameCompanion controller;

    public BoardPane(Board board, GameCompanion controller) {
        this.board = board;
        this.controller = controller;
        board.addListener(this);

        setAlignment(Pos.CENTER);
    }

    private void initialize() {
        double size = 500.0 / Math.max(board.getWidth(), board.getHeight());
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                if (board.hasCell(i, j)) {
                    TilePane tilePane = new TilePane(size, size);
                    tilePane.setOnMouseClicked(controller::selectBoardTile);
                    add(tilePane, i, j);
                } else {
                    Rectangle rectangle = new Rectangle(size, size);
                    rectangle.setOpacity(0);
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
            if (board.hasCell(x, y)) {
                Tile tile = board.getTile(x, y);
                TilePane tilePane = (TilePane) n;
                tilePane.setTile(tile);
                tilePane.setValid(board.isValid(x, y));
                tilePane.setClickeable(board.isValid(x, y) && board.isOnTurn());
            }
        }
    }
}
