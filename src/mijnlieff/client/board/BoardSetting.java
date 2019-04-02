package mijnlieff.client.board;

import java.util.List;

/**
 * Holds information about the shape of the board.
 * Cannot be an enum of presets because the opponent is allowed to choose different ones.
 */
public class BoardSetting {

    public static final BoardSetting DEFAULT = new BoardSetting("0 0 0 2 2 0 2 2");

    private static final List<BoardSetting> presets = List.of(
            DEFAULT,
            new BoardSetting("0 0 0 2 2 1 2 3"),
            new BoardSetting("0 0 0 2 2 2 2 4"),
            new BoardSetting("0 1 2 0 2 2 4 1")
    );

    private int[] x;
    private int[] y;

    public BoardSetting(String string) {
        this.x = new int[4];
        this.y = new int[4];
        String[] args = string.split(" ");
        this.y[0] = Integer.parseInt(args[0]);
        this.x[0] = Integer.parseInt(args[1]);
        this.y[1] = Integer.parseInt(args[2]);
        this.x[1] = Integer.parseInt(args[3]);
        this.y[2] = Integer.parseInt(args[4]);
        this.x[2] = Integer.parseInt(args[5]);
        this.y[3] = Integer.parseInt(args[6]);
        this.x[3] = Integer.parseInt(args[7]);
    }

    public int getX(int i) {
        return x[i];
    }

    public int getY(int i) {
        return y[i];
    }

    public static BoardSetting getPreset(int i) {
        return presets.get(i);
    }

    public static int numPresets() {
        return presets.size();
    }

    public String toString() {
        return String.join(" ",
                String.valueOf(y[0]), String.valueOf(x[0]),
                String.valueOf(y[1]), String.valueOf(x[1]),
                String.valueOf(y[2]), String.valueOf(x[2]),
                String.valueOf(y[3]), String.valueOf(x[3])
        );
    }
}
