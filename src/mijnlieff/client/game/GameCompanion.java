package mijnlieff.client.game;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import mijnlieff.client.Connection;
import mijnlieff.client.board.*;

public class GameCompanion {

    protected BorderPane view;
    protected Label label;

    protected Board model;

    private Connection connection;

    private Deck playerDeck;

    public GameCompanion(Connection connection, BoardSetting boardSetting) {
        model = new Board(connection, boardSetting);
        this.connection = connection;
        initialize();
        model.setCurrentMove(0);
        playerDeck = model.getDeck(model.getPlayer().getColor());
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

    public void selectDeckTile(Player.Color playerColor, int row) {
        if (!model.isOnTurn()) return;
        if (!playerDeck.getPlayerColor().equals(playerColor)) return;

        playerDeck.setSelectedTile(row);
    }

    public void selectBoardTile(MouseEvent e) {
        if (!model.isOnTurn()) return;
        if (playerDeck.getSelectedTile() == -1) return;

        TilePane selectedBoardTile = (TilePane)e.getSource();

        for (int i = 0; i < playerDeck.getTiles().size(); i += 1) {
            Tile t = playerDeck.getTiles().get(i);
            if (playerDeck.getSelectedTile() == i) {
                int x = GridPane.getColumnIndex(selectedBoardTile);
                int y = GridPane.getRowIndex(selectedBoardTile);
                if (model.selectBoardCell(t, x, y)) {
                    playerDeck.removeOneFromDeck(t);
                }
                return;
            }
        }
    }

    public Board getModel() {
        return model;
    }
}












