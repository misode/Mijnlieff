package mijnlieff.client.bot;

import javafx.application.Platform;
import mijnlieff.client.Connection;
import mijnlieff.client.ConnectionListener;
import mijnlieff.client.board.*;
import mijnlieff.client.game.Player;

import java.io.IOException;
import java.util.Random;

public class Bot extends ConnectionListener {

    private String hostName;
    private int portNumber;
    private Random RG;

    public Bot(String hostName, int portNumber) {
        super(new Connection());
        this.RG = new Random();
        this.hostName = hostName;
        this.portNumber = portNumber;
        initialize();
    }

    private void initialize() {
        try {
            connection.start(hostName, portNumber);
            int random = RG.nextInt(1000000);
            connection.identify("Bot" + random);
            System.out.println("Identified with username " + connection.getPlayer().getUsername());
            connection.enqueue();
            connection.setListener(this);
        } catch (IOException e) {
            System.err.println("Failed to make a connection to " + hostName + ":" + portNumber);
            e.printStackTrace();
            Platform.exit();
        }
    }

    public void gameEstablished(Player opponent) {
        System.out.println("Established game with player " + opponent.getUsername());
        if (opponent.getColor() == Player.Color.WHITE) {
            connection.send(BoardSetting.DEFAULT.toString());
            new BotBoard(this, connection, BoardSetting.DEFAULT, RG).setCurrentMove(0);
        }
    }

    public void received(String response) {
        new BotBoard(this, connection, new BoardSetting(response), RG).setCurrentMove(0);
    }

    public void closed() {
        connection = new Connection();
        connection.setListener(this);
        initialize();
    }
}
