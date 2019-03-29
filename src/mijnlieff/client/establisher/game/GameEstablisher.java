package mijnlieff.client.establisher.game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import mijnlieff.client.Connection;
import mijnlieff.client.board.Tile;

import java.util.ArrayList;

/**
 * Companion class for the player list.
 * Used by Mijnlieff to find two players for a game
 */
public class GameEstablisher {
    
    public ListView<String> playerList;
    public Button enqueueButton;
    public Button selectButton;

    private Timeline playerRefresher;
    private Connection connection;
    private GameEstablishedListener listener;
    private boolean enqueued;

    public GameEstablisher() {
        playerRefresher = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> updatePlayerList()));
        playerRefresher.setCycleCount(Timeline.INDEFINITE);
        playerRefresher.play();

        enqueued = false;
    }

    public void initialize() {
        playerList.setCellFactory(PlayerCell::new);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        new Thread(() -> connection.watchGameEstablishing(this)).start();
        updatePlayerList();
    }

    public void setListener(GameEstablishedListener listener) {
        this.listener = listener;
    }

    public void doQueue(ActionEvent actionEvent) {
        enqueueButton.getStyleClass().removeAll("invalid");
        enqueued = !enqueued;
        if(enqueued) {
            connection.enqueue();
            enqueueButton.setText("Dequeue");
        } else {
            connection.dequeue();
            enqueueButton.setText("Enqueue");
        }
    }

    public void doSelect(ActionEvent actionEvent) {
        selectButton.getStyleClass().removeAll("invalid");
        String opponentName = playerList.getSelectionModel().getSelectedItem();
        if(enqueued) doQueue(null);
        connection.selectOpponent(opponentName);
    }

    public void joined(Opponent opponent) {
        playerRefresher.stop();
        listener.establishedGame(opponent);
    }

    public void refreshedPlayerList(ArrayList<String> playerNames) {
        playerList.setItems(FXCollections.observableArrayList(playerNames));
    }

    public boolean isEnqueued() {
        return enqueued;
    }

    private void updatePlayerList() {
        connection.requestPlayerList();
    }

    public static class PlayerCell extends ListCell<String> {

        public PlayerCell(ListView<String> list) {
            setContentDisplay(ContentDisplay.LEFT);
        }

        public void updateItem(String name, boolean empty) {
            super.updateItem(name, empty);
            if(empty) {
                setText(null);
            } else {
                setText(name);
            }
        }
    }
    
    
}
