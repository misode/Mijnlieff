package mijnlieff.client.control;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import mijnlieff.client.board.Board;

public class LastMoveButton extends Button implements InvalidationListener, EventHandler<ActionEvent> {

    private Board model;

    public LastMoveButton() {
        getStyleClass().add("button");
        setOnAction(this);
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
        setDisable(!model.hasNextMove());
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        while(model.setCurrentMove(model.getCurrentMove() + 1));
    }
}
