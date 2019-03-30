package mijnlieff.client;

import mijnlieff.client.board.BoardSetting;
import mijnlieff.client.game.Player;

import java.util.ArrayList;

public abstract class ConnectionListener {

    protected Connection connection;

    public void identified(boolean success) {}

    public void updatePlayerList(ArrayList<String> playerNames) {}

    public void gameEstablished(Player opponent) {}

    public void boardEstablished(BoardSetting boardSetting) {}

    public void receivedMove(String move) {}
}
