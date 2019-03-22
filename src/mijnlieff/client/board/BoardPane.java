package mijnlieff.client.board;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 * View of the board
 */

public class BoardPane extends GridPane implements InvalidationListener {

    private static Image emptyImage = new Image("mijnlieff/client/img/empty.png");

    private Board model;
    private ImageView[][] grid;

    public BoardPane() {
        getStyleClass().add("board-pane");
        setAlignment(Pos.CENTER);
        grid = new ImageView[4][4];
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                ImageView image = new ImageView(emptyImage);
                image.getStyleClass().add("board-image");
                image.setFitHeight(150);
                image.setFitWidth(150);
                add(image, i, j);
                grid[j][i] = image;
            }
        }
    }

    public void setModel(Board model) {
        this.model = model;
        model.addListener(this);
    }

    public Board getModel() {
        return model;
    }

    @Override
    public void invalidated(Observable observable) {
        for(Node n : getChildren()) {
            ImageView cell = (ImageView)n;
            cell.setImage(emptyImage);
        }
        for(Move m : model.getMoves()) {
            ImageView cell = grid[m.getX()][m.getY()];
            cell.setImage(m.getTile().getImage());
        }
    }
}
