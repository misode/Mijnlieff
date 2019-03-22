package mijnlieff.client.board;

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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        return tile.toString() + "(" + x + ", " + y + ")";
    }
}
