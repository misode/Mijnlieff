package mijnlieff.client.bot;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import mijnlieff.client.Connection;
import mijnlieff.client.board.*;

import java.util.Random;

public class BotBoard extends Board {

    private Bot bot;
    private Random RG;
    private Deck botDeck;
    private Deck playerDeck;

    public BotBoard(Bot bot, Connection connection, BoardSetting boardSetting, Random RG) {
        super(connection, boardSetting);
        this.bot = bot;
        this.RG = RG;
        this.botDeck = getDeck(connection.getPlayer().getColor());
        this.playerDeck = getDeck(connection.getOpponent().getColor());
        if(onTurn) {
            doMove();
        }
    }

    public void received(String response) {
        onTurn = true;
        if (response != null) {
            Move move = Move.decode(connection.getPlayer().getColor().next(), response);
            System.out.println("> " + move);
            addMove(move);
            calculateValidCells();
            if (wasBlocked) {
                if (playerDeck.getTiles().size() > 0) {
                    connection.send(null);
                    onTurn = false;
                } else {
                    connection.close();
                    return;
                }
            }
        }
        fireInvalidationEvent();
        if (onTurn) {
            if (botDeck.getTiles().size() > 0) {
                new Timeline(new KeyFrame(Duration.seconds(0.1), e -> doMove())).play();
            } else {
                connection.close();
            }
        }
    }

    private void doMove() {
        Move move = randomMove();
        System.out.println("< " + move);
        addTile(move.getTile(), move.getX(), move.getY());
        botDeck.removeOneFromDeck(move.getTile());
    }

    private Move randomMove() {
        Tile deckTile = botDeck.getTiles().get(RG.nextInt(botDeck.getTiles().size()));
        Move attempt = new Move(RG.nextInt(getWidth()), RG.nextInt(getHeight()), deckTile);
        while (!isValid(attempt.getX(), attempt.getY())) {
            attempt.setX(RG.nextInt(getWidth()));
            attempt.setY(RG.nextInt(getHeight()));
        }
        return attempt;
    }

    public void closed() {
        bot.closed();
    }
}
