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
        System.out.println("initializing viewer companion");
        firstMoveButton = new Button("|<");
        prevMoveButton = new Button("<");
        nextMoveButton = new Button(">");
        lastMoveButton = new Button(">|");

        model.addListener(o -> firstMoveButton.setDisable(model.getCurrentMove() <= 0));
        model.addListener(o -> prevMoveButton.setDisable(model.getCurrentMove() <= 0));
        model.addListener(o -> nextMoveButton.setDisable(!model.hasNextMove()));
        model.addListener(o -> lastMoveButton.setDisable(!model.hasNextMove()));

        firstMoveButton.setOnAction(e -> model.setCurrentMove(0));
        prevMoveButton.setOnAction(e -> model.setCurrentMove(model.getCurrentMove() - 1));
        nextMoveButton.setOnAction(e -> model.setCurrentMove(model.getCurrentMove() + 1));
        lastMoveButton.setOnAction(e -> {while (model.hasNextMove()) {model.setCurrentMove(model.getCurrentMove() + 1);}});

        HBox buttons = new HBox(firstMoveButton, prevMoveButton, nextMoveButton, lastMoveButton);
        buttons.getStyleClass().add("view-control");

        view.setBottom(buttons);
    }
}
