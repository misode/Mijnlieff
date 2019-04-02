package mijnlieff.client.game;

public class Player {

    private String username;
    private Color color;

    public Player(String username, Color color) {
        this.username = username;
        this.color = color;
    }

    public String getUsername() {
        return username;
    }

    public Color getColor() {
        return color;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public enum Color {
        WHITE("white"),
        BLACK("black");

        private String name;
        private Color next;

        static {
            WHITE.next = BLACK;
            BLACK.next = WHITE;
        }

        Color(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Color next() {
            return next;
        }

        public String toString() {
            return name;
        }
    }
}
