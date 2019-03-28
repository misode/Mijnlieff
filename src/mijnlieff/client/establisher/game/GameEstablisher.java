package mijnlieff.client.establisher.game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import mijnlieff.client.Connection;
import mijnlieff.client.board.Tile;

import java.io.IOException;

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
        playerRefresher = new Timeline(new KeyFrame(Duration.seconds(2),
                e -> updatePlayerList()));
        playerRefresher.setCycleCount(Timeline.INDEFINITE);
        playerRefresher.play();

        enqueued = false;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        updatePlayerList();
    }

    public void setListener(GameEstablishedListener listener) {
        this.listener = listener;
    }

    public void initialize() {
        playerList.setCellFactory(PlayerCell::new);
    }

    public void doQueue(ActionEvent actionEvent) {
        enqueueButton.getStyleClass().removeAll("invalid");
        if(enqueued) {
            connection.dequeue();
        } else {
            connection.enqueue(this);
        }
        enqueued = !enqueued;
        if(enqueued) {
            enqueueButton.setText("Enqueue");
        } else {
            enqueueButton.setText("Dequeue");
        }
    }

    public void doSelect(ActionEvent actionEvent) {
        selectButton.getStyleClass().removeAll("invalid");
        String opponentName = playerList.getSelectionModel().getSelectedItem();
        Tile.Player player = connection.selectOpponent(opponentName);
        if(opponentName != null && player != null) {
            joined(new JoinTask.Opponent(player, opponentName));
        } else {
            selectButton.getStyleClass().add("invalid");
        }
    }

    public void joined(JoinTask.Opponent opponent) {
        playerRefresher.stop();
        listener.establishedGame(opponent);
    }

    private void updatePlayerList() {
        ObservableList<String> playerNames = FXCollections.observableArrayList(connection.getPlayerList());
        playerList.setItems(playerNames);
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
