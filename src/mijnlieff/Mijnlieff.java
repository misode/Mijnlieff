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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Mijnlieff extends Application implements ConnectionEstablishedListener, GameEstablishedListener, BoardEstablishedListener {

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

    private void initializeGame() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client/connection.fxml"));
        Scene scene = new Scene(loader.load(), 300, 120);
        ConnectionEstablisher companion = loader.getController();

        changeScene(scene);
        companion.setListener(this);
    }

    @Override
    public void establishedConnection(Connection connection, String username){
        this.connection = connection;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client/playerlist.fxml"));
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

    @Override
    public void establishedGame(JoinTask.Opponent opponent) {
        System.out.println("Established game with player " + opponent.username);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client/boardChooser.fxml"));
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

    private void initializeViewer() throws IOException {
        connection = new Connection(hostName, portNumber);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("client/viewer.fxml"));
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

    private void changeScene(Scene scene) {
        scene.getStylesheets().add("mijnlieff/client/style.css");

        stage.setScene(scene);
        stage.setTitle("Mijnlieff");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

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
