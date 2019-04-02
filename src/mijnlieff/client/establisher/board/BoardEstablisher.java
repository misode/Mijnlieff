package mijnlieff.client.establisher.board;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import mijnlieff.client.Connection;
import mijnlieff.client.ConnectionListener;
import mijnlieff.client.board.BoardSetting;
import mijnlieff.client.game.Player;

/**
 * Companion for the board chooser
 */
public class BoardEstablisher extends ConnectionListener {

    private VBox view;

    private GridPane boardPreviewer;
    private Button prevButton;
    private Button nextButton;

    private Player opponent;
    private BoardEstablishedListener listener;
    private boolean choosingBoard;
    private int selectedPreset;

    public BoardEstablisher(Connection connection, Player opponent, BoardEstablishedListener listener) {
        super(connection);
        this.opponent = opponent;
        this.listener = listener;

        choosingBoard = opponent.getColor() == Player.Color.WHITE;
        initialize();

        if (choosingBoard) {
            selectedPreset = 0;
            prevButton.setDisable(true);
            updatePreview();
        } else {
            connection.waitForBoard();
        }
    }

    private void initialize() {
        Label title = new Label("Playing against");
        title.setFont(new Font(20));
        title.setAlignment(Pos.CENTER);

        Label opponentName = new Label(opponent.getUsername());
        opponentName.setFont(new Font(25));
        opponentName.setAlignment(Pos.CENTER);

        view = new VBox(title, opponentName);

        if (choosingBoard) {
            prevButton = new Button("<");
            prevButton.setOnAction(e -> prevPreset());

            Label boardLabel = new Label("Choose a Board");
            boardLabel.setAlignment(Pos.CENTER);

            boardPreviewer = new GridPane();
            boardPreviewer.setAlignment(Pos.CENTER);

            nextButton = new Button(">");
            nextButton.setOnAction(e -> nextPreset());

            HBox boardChooser = new HBox(
                    prevButton,
                    new VBox(boardLabel, boardPreviewer),
                    nextButton);
            boardChooser.setAlignment(Pos.CENTER);

            Button confirmButton = new Button("Confirm");
            confirmButton.setOnAction(e -> doConfirm());
            confirmButton.setAlignment(Pos.BOTTOM_RIGHT);

            view.getChildren().addAll(boardChooser, confirmButton);
        } else {
            Label placeHolder = new Label("Waiting for opponent to choose board");

            view.getChildren().add(placeHolder);
        }
        view.getStylesheets().add("mijnlieff/client/style.css");
    }

    public Parent asParent() {
        return view;
    }

    private void prevPreset() {
        prevButton.setDisable(false);
        nextButton.setDisable(false);
        if (selectedPreset - 1 <= 0) {
            prevButton.setDisable(true);
            if (selectedPreset - 1 < 0) return;
        }
        selectedPreset -= 1;
        updatePreview();
    }

    private void nextPreset() {
        prevButton.setDisable(false);
        nextButton.setDisable(false);
        if (selectedPreset + 1 >= BoardSetting.numPresets() - 1) {
            nextButton.setDisable(true);
            if (selectedPreset + 1 >= BoardSetting.numPresets()) return;
        }
        selectedPreset += 1;
        updatePreview();
    }

    private void doConfirm() {
        BoardSetting boardSetting = BoardSetting.getPreset(selectedPreset);
        connection.sendBoard(boardSetting);
        listener.establishedBoard(boardSetting);
    }

    public void boardEstablished(BoardSetting boardSetting) {
        listener.establishedBoard(boardSetting);
    }

    private void updatePreview() {
        BoardSetting preset = BoardSetting.getPreset(selectedPreset);
        boardPreviewer.getChildren().clear();
        for (int i = 0; i < 4; i++) {
            int col = preset.getX(i);
            int row = preset.getY(i);
            boardPreviewer.add(new Rectangle(20, 20), col, row);
            boardPreviewer.add(new Rectangle(20, 20), col+1, row);
            boardPreviewer.add(new Rectangle(20, 20), col, row+1);
            boardPreviewer.add(new Rectangle(20, 20), col+1, row+1);
        }
    }
}
