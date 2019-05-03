package mijnlieff.client.game;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import mijnlieff.client.Connection;
import mijnlieff.client.board.BoardSetting;

public class ViewerCompanion extends GameCompanion {

    private Button firstMoveButton;
    private Button prevMoveButton;
    private Button nextMoveButton;
    private Button lastMoveButton;

    public ViewerCompanion(Connection connection, BoardSetting boardSetting) {
        super(connection, boardSetting);
    }

    protected void initialize() {
        super.initialize();
        firstMoveButton = new Button("|<");
        prevMoveButton = new Button("<");
        nextMoveButton = new Button(">");
        lastMoveButton = new Button(">|");

        board.addListener(o -> firstMoveButton.setDisable(board.getCurrentMove() <= 0));
        board.addListener(o -> prevMoveButton.setDisable(board.getCurrentMove() <= 0));
        board.addListener(o -> nextMoveButton.setDisable(!board.hasNextMove()));
        board.addListener(o -> lastMoveButton.setDisable(!board.hasNextMove()));

        firstMoveButton.setOnAction(e -> board.setCurrentMove(0));
        prevMoveButton.setOnAction(e -> board.setCurrentMove(board.getCurrentMove() - 1));
        nextMoveButton.setOnAction(e -> board.setCurrentMove(board.getCurrentMove() + 1));
        lastMoveButton.setOnAction(e -> {while (board.hasNextMove()) {
            board.setCurrentMove(board.getCurrentMove() + 1);}});

        HBox buttons = new HBox(firstMoveButton, prevMoveButton, nextMoveButton, lastMoveButton);
        buttons.getStyleClass().add("view-control");

        view.setBottom(buttons);
    }
}
