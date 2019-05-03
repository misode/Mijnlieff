package mijnlieff.client.game;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import mijnlieff.client.Connection;
import mijnlieff.client.board.*;

public class GameCompanion implements InvalidationListener {

    protected BorderPane view;

    private Label whiteScore;
    private Label blackScore;

    protected Board board;
    private Deck playerDeck;

    public GameCompanion(Connection connection, BoardSetting boardSetting) {
        board = new Board(connection, boardSetting);
        board.addListener(this);
        initialize();
        board.setCurrentMove(0);
        playerDeck = board.getDeck(board.getPlayer().getColor());
    }

    protected void initialize() {
        BoardPane boardPane = new BoardPane(board, this);
        DeckPane whiteDeck = new DeckPane(board.getDeck(Player.Color.WHITE), this);
        DeckPane blackDeck = new DeckPane(board.getDeck(Player.Color.BLACK), this);
        board.addListener(boardPane);
        board.addListener(whiteDeck);
        board.addListener(blackDeck);

        whiteScore = new Label("0");
        blackScore = new Label("0");

        AnchorPane header = new AnchorPane(whiteScore, blackScore);
        header.getStyleClass().add("header");
        AnchorPane.setLeftAnchor(whiteScore, 20.0);
        AnchorPane.setRightAnchor(blackScore, 20.0);

        view = new BorderPane();
        view.setLeft(whiteDeck);
        view.setRight(blackDeck);
        view.setCenter(boardPane);
        view.setTop(header);
        view.getStylesheets().add("mijnlieff/client/style.css");
    }

    public Parent asParent() {
        return view;
    }

    public void selectDeckTile(Deck deck, int row) {
        if (!board.isOnTurn()) return;
        if (!playerDeck.getPlayerColor().equals(deck.getPlayerColor())) return;

        playerDeck.setSelectedTile(row);
    }

    public void selectBoardTile(MouseEvent e) {
        if (!board.isOnTurn()) return;
        if (playerDeck.getSelectedTile() == -1) return;

        TilePane selectedBoardTile = (TilePane)e.getSource();
        Tile selectedDeckTile = playerDeck.getTiles().get(playerDeck.getSelectedTile());

        int x = GridPane.getColumnIndex(selectedBoardTile);
        int y = GridPane.getRowIndex(selectedBoardTile);
        if (board.isValid(x, y)) {
            board.addTile(selectedDeckTile, x, y);
            playerDeck.removeOneFromDeck(selectedDeckTile);
        }
    }

    public Board getBoard() {
        return board;
    }

    public void invalidated(Observable o) {
        whiteScore.setText(String.valueOf(calculateScore(Player.Color.WHITE)));
        blackScore.setText(String.valueOf(calculateScore(Player.Color.BLACK)));
    }

    private int calculateScore(Player.Color playerColor) {
        int score = 0;
        int n = Math.max(board.getWidth(), board.getHeight());

        int[] horizontalCount = new int[n];
        int[] verticalCount = new int[n];
        int[] diagonal1Count = new int[2*n];
        int[] diagonal2Count = new int[2*n];
        for (int i = 0; i < n; i += 1) {
            for (int j = 0; j < n; j += 1) {
                Tile tile = board.getTile(i, j);
                if (tile != null && tile.getPlayer() == playerColor) {
                    horizontalCount[i] += 1;
                    verticalCount[j] += 1;
                    diagonal1Count[i + j] += 1;
                    diagonal2Count[i - j + n] += 1;
                }
            }
        }
        for (int i = 0; i < n; i += 1) {
            if (horizontalCount[i] > 2) score += horizontalCount[i] - 2;
            if (verticalCount[i] > 2) score += verticalCount[i] - 2;
        }
        for (int i = 0; i < 2*n; i += 1) {
            if (diagonal1Count[i] > 2) score += diagonal1Count[i] - 2;
            if (diagonal2Count[i] > 2) score += diagonal2Count[i] - 2;
        }

        return score;
    }
}












