package mijnlieff.client;

import mijnlieff.client.board.Tile;
import java.io.IOException;

public interface GameEstablishedListener {

    void establishedGame(JoinTask.Opponent opponent);
}
