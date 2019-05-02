package mijnlieff.client.board;

import mijnlieff.client.game.Player;

import java.util.function.Predicate;

public class Tile {

    private Player.Color player;
    private Type type;

    public Tile(Player.Color player, Type type) {
        this.player = player;
        this.type = type;
    }

    public Player.Color getPlayer() {
        return player;
    }

    public Type getType() {
        return type;
    }

    public String toString() {
        return player.toString() + "-" + type.toString();
    }

    public boolean equals(Tile t) {
        return this.player == t.player && this.type == t.type;
    }

    public enum Type {
        TOREN("toren", "+", p -> (p.x == 0 || p.y == 0)),
        LOPER("loper", "X", p -> (p.x == -p.y || p.x == p.y)),
        PUSHER("pusher", "@", p -> (p.x > 1 || p.x < -1 || p.y > 1 || p.y < -1)),
        PULLER("puller", "o", p -> (p.x <= 1 && p.x >= -1 && p.y <= 1 && p.y >= -1));

        private String name;
        private String chr;
        private Predicate<Pos> allowed;

        Type(String name, String chr, Predicate<Pos> allowed) {
            this.name = name;
            this.chr = chr;
            this.allowed = allowed;
        }

        public boolean isAllowed(int x, int y) {
            return allowed.test(new Pos(x, y));
        }

        public static Type fromChar(String str) {
            if (str.equals("+")) return TOREN;
            if (str.equals("X")) return LOPER;
            if (str.equals("@")) return PUSHER;
            if (str.equals("o")) return PULLER;
            return null;
        }

        public String getChar() {
            return chr;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

        private class Pos {

            private int x, y;

            private Pos(int x, int y) {
                this.x = x;
                this.y = y;
            }
        }
    }
}
