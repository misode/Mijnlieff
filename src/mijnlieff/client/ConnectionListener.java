package mijnlieff.client;

import mijnlieff.client.game.Player;

import java.util.ArrayList;

public abstract class ConnectionListener {

    protected Connection connection;

    public ConnectionListener(Connection connection) {
        this.connection = connection;
        connection.setListener(this);
    }

    public void identified(boolean success) {}

    public void updatePlayerList(ArrayList<String> playerNames) {}

    public void gameEstablished(Player opponent) {}

    public void received(String response) {}
}
