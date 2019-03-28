package mijnlieff.client;

import javafx.concurrent.Task;
import mijnlieff.client.board.Tile;

import java.io.BufferedReader;

/**
 * Task to listen when the player is chosen by an opponent
 */
public class JoinTask extends Task<JoinTask.Opponent> {

    private BufferedReader in;

    public JoinTask(BufferedReader in) {
        this.in = in;
    }

    @Override
    protected Opponent call() throws Exception {
        //String response = in.readLine();
        // TODO: remove temporary response
        Thread.sleep(10000);
        String response = "+ T TheMaster";
        Tile.Player player;
        if(response.charAt(2) == 'T') player = Tile.Player.BLACK;
        else player = Tile.Player.BLACK;
        return new Opponent(player, response.substring(4));
    }

    /**
     * DTO used as result of this task
     */
    public static class Opponent {
        public Tile.Player player;
        public String username;

        public Opponent(Tile.Player player, String username) {
            this.player = player;
            this.username = username;
        }
    }
}
