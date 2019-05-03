package mijnlieff;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import mijnlieff.client.*;
import mijnlieff.client.board.Board;
import mijnlieff.client.bot.Bot;
import mijnlieff.client.establisher.board.BoardEstablishedListener;
import mijnlieff.client.establisher.board.BoardEstablisher;
import mijnlieff.client.board.BoardSetting;
import mijnlieff.client.establisher.connection.ConnectionEstablishedListener;
import mijnlieff.client.establisher.connection.ConnectionEstablisher;
import mijnlieff.client.game.GameCompanion;
import mijnlieff.client.establisher.game.GameEstablishedListener;
import mijnlieff.client.establisher.game.GameEstablisher;
import mijnlieff.client.game.Player;
import mijnlieff.client.game.ViewerCompanion;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Mijnlieff extends Application implements ConnectionEstablishedListener,
        GameEstablishedListener, BoardEstablishedListener {

    private String hostName;
    private int portNumber;
    private String screenshot;
    private Connection connection;
    private Stage stage;
    private Mode mode;

    private enum Mode {
        GAME,
        VIEWER,
        VIEWER_TEST,
        BOT,
        INVALID
    }

    @Override
    public void init() {
        List<String> argList = getParameters().getRaw();

        mode = Mode.INVALID;
        if (argList.size() == 0) {
            mode = Mode.GAME;
        } else if (argList.size() == 1 && argList.get(0).equals("bot")) {
            mode = Mode.BOT;
        } else if (argList.size() == 2 || argList.size() == 3) {
            mode = Mode.VIEWER;
            hostName = argList.get(0);
            portNumber = Integer.parseInt(argList.get(1));
            if (argList.size() == 3) {
                mode = Mode.VIEWER_TEST;
                screenshot = argList.get(2);
            }
        }
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Mijnlieff");

        if (mode == Mode.GAME) initializeGame();
        if (mode == Mode.BOT) initializeBot();
        if (mode == Mode.VIEWER || mode == Mode.VIEWER_TEST) initializeViewer();
        if (mode == Mode.INVALID) Platform.exit();

        if (mode != Mode.BOT) stage.show();
    }

    /**
     * Initializes the game stage to establish a connection
     */
    private void initializeGame() {
        ConnectionEstablisher controller = new ConnectionEstablisher(this);
        Scene scene = new Scene(controller.asParent(), 300, 120);
        stage.setScene(scene);
    }

    /**
     * Stores the connection and changes the stage to establish a game from a playerlist.
     * @param connection the connection that was just established
     * @see Connection
     */
    @Override
    public void establishedConnection(Connection connection){
        this.connection = connection;
        GameEstablisher controller = new GameEstablisher(connection, this);
        Scene scene = new Scene(controller.asParent(), 350, 450);
        double centerX = stage.getX() + stage.getWidth()/2;
        stage.setX(centerX - scene.getWidth()/2);
        stage.setScene(scene);
    }

    /**
     * Changes the stage to establish a board setting after an opponent has been decided.
     * @param opponent the opponent of the game
     * @see Player
     */
    @Override
    public void establishedGame(Player opponent) {
        System.out.println("Established game with player " + opponent.getUsername());
        BoardEstablisher boardEstablisher = new BoardEstablisher(connection, opponent, this);
        Scene scene = new Scene(boardEstablisher.asParent(), 512, 320);
        double centerX = stage.getX() + stage.getWidth()/2;
        stage.setX(centerX - scene.getWidth()/2);
        stage.setScene(scene);
    }

    /**
     * Changes the stage to show the board of the game after the board setting has been decided.
     * @param boardSetting the board setting of the game
     * @see BoardSetting
     */
    @Override
    public void establishedBoard(BoardSetting boardSetting) {
        GameCompanion controller = new GameCompanion(connection, boardSetting);
        Scene scene = new Scene(controller.asParent(), 810, 660);
        double centerX = stage.getX() + stage.getWidth()/2;
        stage.setX(centerX - scene.getWidth()/2);
        stage.setScene(scene);
    }

    private void initializeBot() {
        Bot bot = new Bot("localhost", 4444);
    }

    /**
     * Initializes the viewer by creating a new {@link Connection} and loading a viewer stage.
     * If screenshot is not null, this will also take a snapshot of the scene and write it to the screenshot path
     * @see Connection
     */
    private void initializeViewer() {
        try {
            connection = new Connection();
            connection.start(hostName, portNumber);
        } catch (IOException e) {
            System.err.println("Failed to make a connection to " + hostName + ":" + portNumber);
            e.printStackTrace();
            Platform.exit();
        }
        ViewerCompanion controller = new ViewerCompanion(connection, BoardSetting.DEFAULT);
        Scene scene = new Scene(controller.asParent(), 810, 680);
        stage.setScene(scene);

        if (screenshot != null) {
            Board board = controller.getBoard();
            while (board.hasNextMove()) {
                board.setCurrentMove(board.getCurrentMove() + 1);
            }

            try {
                WritableImage snapshot = scene.snapshot(new WritableImage((int)scene.getWidth(), (int)scene.getHeight()));
                BufferedImage img = SwingFXUtils.fromFXImage(snapshot, null);
                ImageIO.write(img, "png", new File(screenshot));
            } catch (IOException e) {
                e.printStackTrace();
            }

            Platform.exit();
        }
    }

    /**
     * Called by JavaFX when stopping the application, closes the connection
     */
    @Override
    public void stop() {
        if (connection != null) {
            System.err.println("Close 5");
            connection.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
