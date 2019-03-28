package mijnlieff;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import mijnlieff.client.*;
import mijnlieff.client.board.Board;
import mijnlieff.client.establisher.board.BoardEstablishedListener;
import mijnlieff.client.establisher.board.BoardEstablisher;
import mijnlieff.client.establisher.board.BoardSetting;
import mijnlieff.client.establisher.connection.ConnectionEstablishedListener;
import mijnlieff.client.establisher.connection.ConnectionEstablisher;
import mijnlieff.client.establisher.game.GameCompanion;
import mijnlieff.client.establisher.game.GameEstablishedListener;
import mijnlieff.client.establisher.game.GameEstablisher;
import mijnlieff.client.establisher.game.JoinTask;
import mijnlieff.client.viewer.ViewerCompanion;

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
    private Mode mode = Mode.INVALID;

    private enum Mode {
        GAME,
        VIEWER,
        VIEWER_TEST,
        INVALID
    }

    @Override
    public void init() {
        List<String> argList = getParameters().getRaw();

        if(argList.size() == 0) {
            mode = Mode.GAME;
        } else if(argList.size() == 2 || argList.size() == 3) {
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
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        if(mode == Mode.GAME) initializeGame();
        if(mode == Mode.VIEWER || mode == Mode.VIEWER_TEST) initializeViewer();
        if(mode == Mode.INVALID) Platform.exit();
    }

    /**
     * Initializes the game stage to establish a connection
     * @throws IOException
     */
    private void initializeGame() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client/establisher/connection/connection.fxml"));
        Scene scene = new Scene(loader.load(), 300, 120);
        ConnectionEstablisher companion = loader.getController();

        changeScene(scene);
        companion.setListener(this);
    }

    /**
     * Stores the connection and changes the stage to establish a game from a playerlist.
     * @param connection the connection that was just established
     * @param username the username that was chosen by the user
     * @see Connection
     */
    @Override
    public void establishedConnection(Connection connection, String username){
        this.connection = connection;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client/establisher/game/playerlist.fxml"));
        try {
            Scene scene = new Scene(loader.load(), 350, 500);
            GameEstablisher companion = loader.getController();
            companion.setConnection(connection);
            companion.setListener(this);
            changeScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    /**
     * Changes the stage to establish a board setting after an opponent has been decided.
     * @param opponent the opponent of the game
     * @see JoinTask.Opponent
     */
    @Override
    public void establishedGame(JoinTask.Opponent opponent) {
        System.out.println("Established game with player " + opponent.username);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client/establisher/board/boardChooser.fxml"));
        try {
            Scene scene = new Scene(loader.load(), 512, 315);
            BoardEstablisher companion = loader.getController();
            companion.setConnection(connection);
            companion.setListener(this);
            companion.setOpponent(opponent);
            changeScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    /**
     * Changes the stage to show the board of the game after the board setting has been decided.
     * @param boardSetting the board setting of the game
     * @see BoardSetting
     */
    @Override
    public void establishedBoard(BoardSetting boardSetting) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client/game.fxml"));
        try {
            Scene scene = new Scene(loader.load(), 810, 660);
            GameCompanion companion = loader.getController();
            companion.setConnection(connection);
            changeScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the viewer by creating a new {@Link Connection} and loading a viewer stage.
     * If screenshot is not null, this will also take a snapshot of the scene and write it to the screenshot path
     * @throws IOException
     * @see Connection
     */
    private void initializeViewer() throws IOException {
        connection = new Connection(hostName, portNumber);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("client/viewer/viewer.fxml"));
        Scene scene = new Scene(loader.load(), 810, 680);
        ViewerCompanion companion = loader.getController();
        companion.setConnection(connection);

        changeScene(scene);
        companion.setScene(scene);

        if(screenshot != null) {
            Board board = companion.getModel();
            while(board.setCurrentMove(board.getCurrentMove() + 1));

            WritableImage snapshot = scene.snapshot(new WritableImage((int)scene.getWidth(), (int)scene.getHeight()));
            BufferedImage img = SwingFXUtils.fromFXImage(snapshot, null);
            ImageIO.write(img, "png", new File(screenshot));

            Platform.exit();
        }
    }

    /**
     * Updates the initial stage with a new scene
     * @param scene the new scene to apply
     * @see Scene
     */
    private void changeScene(Scene scene) {
        scene.getStylesheets().add("mijnlieff/client/style.css");

        stage.setScene(scene);
        stage.setTitle("Mijnlieff");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Called by JavaFX when stopping the application, closes the connection
     * @throws IOException
     */
    @Override
    public void stop() throws IOException {
        if(connection != null) {
            connection.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
