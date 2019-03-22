package mijnlieff;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import mijnlieff.client.Companion;
import mijnlieff.client.EchoClient;
import mijnlieff.client.board.Board;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Mijnlieff extends Application {

    private String hostName;
    private int portNumber;
    private String screenshot;
    private EchoClient echoClient;

    @Override
    public void init() {
        List<String> argList = getParameters().getRaw();
        hostName = argList.get(0);
        portNumber = Integer.parseInt(argList.get(1));
        if(argList.size() == 3) {
            screenshot = argList.get(2);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        echoClient = new EchoClient(hostName, portNumber);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("client/viewer.fxml"));
        Scene scene = new Scene(loader.load(), 810, 680);
        scene.getStylesheets().add("mijnlieff/client/style.css");
        Companion companion = loader.getController();
        companion.setEchoClient(echoClient);

        stage.setScene(scene);
        stage.setTitle("Mijnlieff");
        stage.setResizable(false);
        stage.show();

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

    @Override
    public void stop() throws IOException {
        echoClient.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
