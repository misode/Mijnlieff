package mijnlieff.client.board;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import mijnlieff.client.Connection;
import mijnlieff.client.ConnectionListener;
import mijnlieff.client.game.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model of the board
 */
public class Board extends ConnectionListener implements Observable {

    private ArrayList<Move> moves;
    private int width, height;
    private boolean[][] cells;
    private boolean[][] valid;
    private int currentMove;
    private boolean reachedEnd;
    private boolean onTurn;
    private boolean wasBlocked;

    private Deck whiteDeck;
    private Deck blackDeck;

    public Board(Connection connection, BoardSetting boardSetting) {
        super(connection);

        moves = new ArrayList<>();
        width = 0;
        height = 0;
        currentMove = -1;
        reachedEnd = false;
        onTurn = false;
        wasBlocked = false;
        whiteDeck = new Deck(Player.Color.WHITE, this);
        blackDeck = new Deck(Player.Color.BLACK, this);

        // In the viewer mode the player's color will be null meaning onTurn will stay false
        onTurn = connection.getPlayer().getColor() == Player.Color.WHITE;

        for (int i = 0; i < 4; i++) {
            if (boardSetting.getX(i) + 2 > width) width = boardSetting.getX(i) + 2;
            if (boardSetting.getY(i) + 2 > height) height = boardSetting.getY(i) + 2;
        }
        cells = new boolean[width][height];
        valid = new boolean[width][height];
        for (int i = 0; i < 4; i++) {
            int x = boardSetting.getX(i);
            int y = boardSetting.getY(i);
            cells[x][y] = true;
            cells[x+1][y] = true;
            cells[x][y+1] = true;
            cells[x+1][y+1] = true;

            valid[x][y] = true;
            valid[x+1][y] = true;
            valid[x][y+1] = true;
            valid[x+1][y+1] = true;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Deck getDeck(Player.Color playerColor) {
        if (playerColor == Player.Color.WHITE) return whiteDeck;
        else return blackDeck;
    }

    public Player getPlayer() {
        return connection.getPlayer();
    }

    /**
     * Updates the board when the server sends a move.
     * @param response the response from the server containing the information about the move
     */
    public void received(String response) {
        onTurn = true;
        if(response != null) {
            Move move = Move.decode(connection.getPlayer().getColor().next(), response);
            if (!isValid(move.getX(), move.getY())) {
                System.err.println("Your opponent was cheating!");
                Platform.exit();
                return;
            }
            addMove(move);
            calculateValidCells();
            if(wasBlocked) {
                connection.send(null);
                onTurn = false;
            }
        }
        fireInvalidationEvent();
    }

    /**
     * Adds a tile to the board and notifies the server about this
     * @param tile the tile to be added to the board
     * @param x the x destination on the board
     * @param y the y destination on the board
     */
    public void addTile(Tile tile, int x, int y) {
        Move move = new Move(x, y, tile);
        addMove(move);
        connection.send(move.encode());
        onTurn = false;
        calculateValidCells();
        fireInvalidationEvent();
    }

    /**
     * Checks whether the specified position is a valid cell to place a tile.
     * @param x the x position of the cell
     * @param y the y position of the cell
     * @return true if a new tile is allowed in this cell
     */
    public boolean isValid(int x, int y) {
        return valid[x][y] || wasBlocked;
    }

    public boolean isOnTurn() {
        return onTurn;
    }

    /**
     * Updates the current move.
     *
     * If this move is not available in the stored buffer,
     * it will request the next move to the server.
     *
     * If the current move was changed, this will fire an invalidation event.
     * @param newMove the new move index t
     */
    public void setCurrentMove(int newMove) {
        if (currentMove == newMove) return;
        if (newMove > moves.size()) {
            if (reachedEnd) return;
            requestMove();
            currentMove = newMove;
        } else {
            currentMove = newMove;
            if (currentMove < 0) {
                currentMove = 0;
            }
        }
        fireInvalidationEvent();
    }

    public int getCurrentMove() {
        return currentMove;
    }

    public boolean hasNextMove() {
        return !(reachedEnd && getCurrentMove() >= moves.size());
    }

    /**
     * Gets the list of moves upto the current move
     * @return a list of moves
     * @see Move
     */
    public List<Move> getMoves() {
        return moves.subList(0, currentMove);
    }

    /**
     * Checks if the board has a cell at the specified position.
     * @param x the x position of the cell
     * @param y the y position of the cell
     * @return true if the board has a cell at the specified location
     */
    public boolean hasCell(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return false;
        return cells[x][y];
    }

    /**
     * Gets the cell at the current move.
     *
     * @param x the x position of the cell
     * @param y the y position of the cell
     * @return null if the cell is empty
     * @see Tile
     */
    public Tile getTile(int x, int y) {
        for (Move m : getMoves()) {
            if (m.getX() == x && m.getY() == y) {
                return m.getTile();
            }
        }
        return null;
    }

    /**
     * Adds a move to the board
     * @param move the new move to be added
     */
    private void addMove(Move move) {
        moves.add(move);
        currentMove += 1;
        fireInvalidationEvent();
    }

    /**
     * Request the next move from the server.
     *
     * Only used in part 1
     */
    private void requestMove() {
        Player.Color player = Player.Color.WHITE;
        if (moves.size() > 0) {
            player = moves.get(moves.size() - 1).getTile().getPlayer();
            if (!wasBlocked) {
                player = player.next();
            }
        }
        connection.send(null);
        String response = connection.read().substring(2);
        Move newMove = Move.decode(player, response);
        if (response.substring(0, 1).equals("T")) {
            reachedEnd = true;
        }
        moves.add(newMove);
    }

    private void calculateValidCells() {
        wasBlocked = true;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                valid[i][j] = isValidCell(i, j);
                if (valid[i][j]) {
                    wasBlocked = false;
                }
            }
        }
    }

    private boolean isValidCell(int x, int y) {
        if (moves.size() > 0) {
            Move lastMove = moves.get(moves.size() - 1);
            if (!lastMove.getTile().getType().isAllowed(x - lastMove.getX(), y - lastMove.getY())) {
                return false;
            }
        }
        return hasCell(x, y) && getTile(x, y) == null;
    }

    private List<InvalidationListener> listenerList = new ArrayList<>();

    private void fireInvalidationEvent () {
        for (InvalidationListener listener : listenerList) {
            listener.invalidated(this);
        }
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.listenerList.add(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.listenerList.remove(invalidationListener);
    }

    public String toString() {
        return "[" + moves.stream().limit(currentMove).map(Move::toString).collect(Collectors.joining(", ")) + "]";
    }
}
