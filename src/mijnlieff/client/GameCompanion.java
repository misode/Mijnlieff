package mijnlieff.client;

import mijnlieff.client.establisher.board.BoardSetting;

public class GameCompanion {

    private Connection connection;
    private BoardSetting boardSetting;

    public void initialize() {

    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setBoardSetting(BoardSetting boardSetting) {
        this.boardSetting = boardSetting;
    }
}
