package mijnlieff.client.game;

import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import mijnlieff.client.Connection;
import mijnlieff.client.board.*;

public class GameCompanion {

    protected BorderPane view;

    protected Board model;

    private ImageView selectedDeckTile;

    public GameCompanion(Connection connection, BoardSetting boardSetting) {
        model = new Board(connection, boardSetting);
        initialize();
        model.resetCurrentMove();
    }

    protected void initialize() {
        BoardPane boardPane = new BoardPane(model, this);
        DeckPane whiteDeck = new DeckPane(model.getDeck(Player.Color.WHITE), this);
        DeckPane blackDeck = new DeckPane(model.getDeck(Player.Color.BLACK), this);
        model.addListener(boardPane);
        model.addListener(whiteDeck);
        model.addListener(blackDeck);

        view = new BorderPane();
        view.setLeft(whiteDeck);
        view.setRight(blackDeck);
        view.setCenter(boardPane);
        view.getStylesheets().add("mijnlieff/client/style.css");
    }

    public Parent asParent() {
        return view;
    }

    public void selectDeckTile(MouseEvent e) {
        if(!model.isOnTurn()) return;

        selectedDeckTile = (ImageView)e.getSource();
    }

    public void selectBoardTile(MouseEvent e) {
        if(!model.isOnTurn()) return;
        if(selectedDeckTile == null) return;

        ImageView selectedBoardTile = (ImageView)e.getSource();

        for(Tile t : model.getDeck(model.getPlayer().getColor()).getTiles()) {
            if(t.getImage().equals(selectedDeckTile.getImage())) {
                int x = GridPane.getColumnIndex(selectedBoardTile);
                int y = GridPane.getRowIndex(selectedBoardTile);
                model.transferTile(t, x, y);
                return;
            }
        }
    }

    public Board getModel() {
        return model;
    }
}
