package mijnlieff.client.game;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import mijnlieff.client.Connection;
import mijnlieff.client.board.*;

public class GameCompanion implements InvalidationListener {

    protected BorderPane view;
    protected Board model;

    private Deck playerDeck;

    public GameCompanion(Connection connection, BoardSetting boardSetting) {
        model = new Board(connection, boardSetting);
        model.addListener(this);
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

    public void selectDeckTile(Deck deck, int row) {
        if (!model.isOnTurn()) return;
        if (!playerDeck.getPlayerColor().equals(deck.getPlayerColor())) return;

        playerDeck.setSelectedTile(row);
    }

    public void selectBoardTile(MouseEvent e) {
        if (!model.isOnTurn()) return;
        if (playerDeck.getSelectedTile() == -1) return;

        TilePane selectedBoardTile = (TilePane)e.getSource();
        Tile selectedDeckTile = playerDeck.getTiles().get(playerDeck.getSelectedTile());

        int x = GridPane.getColumnIndex(selectedBoardTile);
        int y = GridPane.getRowIndex(selectedBoardTile);
        if (model.isValid(x, y)) {
            model.addTile(selectedDeckTile, x, y);
            playerDeck.removeOneFromDeck(selectedDeckTile);
        }
    }

    public Board getModel() {
        return model;
    }

    public void invalidated(Observable o) {
        int blackDeck = model.getDeck(Player.Color.BLACK).getTiles().size();
        int whiteDeck = model.getDeck(Player.Color.WHITE).getTiles().size();

        if (whiteDeck > 0 && blackDeck > 0) return;


    }
}












