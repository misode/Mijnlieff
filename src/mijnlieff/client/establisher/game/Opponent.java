package mijnlieff.client.establisher.game;

import mijnlieff.client.board.Tile;

public class Opponent {

    private Tile.Player player;
    private String username;

    public Opponent(Tile.Player player, String username) {
        this.player = player;
        this.username = username;
    }

    public Tile.Player getPlayer() {
        return player;
    }

    public String getUsername() {
        return username;
    }
}
