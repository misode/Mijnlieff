package mijnlieff.client.board;

import mijnlieff.client.game.Player;

public class Move {

    private int x, y;
    private Tile tile;

    public Move(int x, int y, Tile tile) {
        this.x = x;
        this.y = y;
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        return tile.toString() + "(" + x + ", " + y + ")";
    }

    public String encode() {
        String message = "F";
        message += " " + getY();
        message += " " + getX();
        message += " " + getTile().getType().getChar();
        return message;
    }

    public static Move decode(Player.Color playerColor, String string) {
        String[] msg = string.split(" ");
        // converting rows/columns to x/y
        int moveY = Integer.parseInt(msg[1]);
        int moveX = Integer.parseInt(msg[2]);
        Tile.Type type = Tile.Type.fromChar(msg[3]);
        Tile newTile = new Tile(playerColor, type);
        return new Move(moveX, moveY, newTile);
    }
}
