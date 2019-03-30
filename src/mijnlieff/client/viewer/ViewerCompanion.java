package mijnlieff.client.viewer;

import javafx.scene.Scene;
import mijnlieff.client.Connection;
import mijnlieff.client.board.Board;
import mijnlieff.client.board.BoardSetting;
import mijnlieff.client.game.GameCompanion;

public class ViewerCompanion {

    public Board model;

    public void initialize() {
        model.setBoardSetting(BoardSetting.DEFAULT);
        model.resetCurrentMove();
    }

    public Board getModel() {
        return model;
    }

    public void setConnection(Connection connection) {
        model.setConnection(connection);
    }

    public void setScene(Scene scene) {
        GameCompanion.initializeDecks(scene, model);
    }
}
