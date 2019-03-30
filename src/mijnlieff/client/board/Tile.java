package mijnlieff.client.board;

import javafx.scene.image.Image;

import java.util.function.Predicate;

public class Tile {

    private Player player;
    private Type type;
    private Image image;

    public Tile(Player player, Type type) {
        this.player = player;
        this.type = type;
        this.image = new Image("mijnlieff/client/img/" + player.getName() + "-" + type.getName() + ".png");
    }

    public Player getPlayer() {
        return player;
    }

    public Type getType() {
        return type;
    }

    public Image getImage() {
        return image;
    }

    public String toString() {
        return player.toString() + "-" + type.toString();
    }

    public boolean equals(Tile t) {
        return this.player == t.player && this.type == t.type;
    }

    public enum Player {
        WHITE("white"),
        BLACK("black");

        private String name;
        private Player other;

        static {
            WHITE.other = BLACK;
            BLACK.other = WHITE;
        }

        Player(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Player other() {
            return other;
        }

        public String toString() {
            return name;
        }
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
            if(str.equals("+")) return TOREN;
            if(str.equals("X")) return LOPER;
            if(str.equals("@")) return PUSHER;
            if(str.equals("o")) return PULLER;
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
