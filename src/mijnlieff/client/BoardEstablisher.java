package mijnlieff.client;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import mijnlieff.client.board.Tile;

/**
 * Companion for the board chooser
 */
public class BoardEstablisher {

    public Label opponentName;
    public AnchorPane boardChooser;
    public GridPane boardPreviewer;
    public Button prevButton;
    public Button nextButton;
    public Button confirmButton;
    public Label placeHolder;

    private Connection connection;
    private BoardEstablishedListener listener;
    private boolean choosingBoard;
    private int selectedPreset;
    private BoardEstablisherTask waitTask;

    public void initialize() {
        boardPreviewer.setAlignment(Pos.CENTER);
        choosingBoard = false;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setListener(BoardEstablishedListener listener) {
        this.listener = listener;
    }

    public void setOpponent(JoinTask.Opponent opponent) {
        opponentName.setText(opponent.username);
        choosingBoard = opponent.player == Tile.Player.WHITE;
        placeHolder.setVisible(!choosingBoard);
        boardChooser.setVisible(choosingBoard);

        if(choosingBoard) {
            selectedPreset = 0;
            prevButton.setDisable(true);
            updatePreview();
        } else {
            connection.waitBoard(this);
        }
    }

    public void prevPreset(ActionEvent actionEvent) {
        prevButton.setDisable(false);
        nextButton.setDisable(false);
        if(selectedPreset - 1 <= 0) {
            prevButton.setDisable(true);
            if(selectedPreset - 1 < 0) return;
        }
        selectedPreset -= 1;
        updatePreview();

    }

    public void nextPreset(ActionEvent actionEvent) {
        prevButton.setDisable(false);
        nextButton.setDisable(false);
        if(selectedPreset + 1 >= BoardSetting.numPresets() - 1) {
            nextButton.setDisable(true);
            if(selectedPreset + 1 >= BoardSetting.numPresets()) return;
        }
        selectedPreset += 1;
        updatePreview();
    }

    public void selectedBoard(BoardSetting boardSetting) {
        listener.establishedBoard(boardSetting);
    }

    public void confirm(ActionEvent actionEvent) {
        BoardSetting boardSetting = BoardSetting.getPreset(selectedPreset);
        connection.decideBoard(boardSetting.toString());
        listener.establishedBoard(boardSetting);
    }

    private void updatePreview() {
        BoardSetting preset = BoardSetting.getPreset(selectedPreset);
        boardPreviewer.getChildren().clear();
        for(int i = 0; i < 4; i++) {
            int x = preset.getX(i);
            int y = preset.getY(i);
            boardPreviewer.add(new Rectangle(20, 20), y, x);
            boardPreviewer.add(new Rectangle(20, 20), y+1, x);
            boardPreviewer.add(new Rectangle(20, 20), y, x+1);
            boardPreviewer.add(new Rectangle(20, 20), y+1, x+1);
        }
    }
}
