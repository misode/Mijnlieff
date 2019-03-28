package mijnlieff.client;

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
            try {
                connection.dequeue();
            } catch (IOException e) {
                enqueueButton.getStyleClass().add("invalid");
                System.err.println("Unable to dequeue");
                e.printStackTrace();
            }
        } else {
            try {
                connection.enqueue(this);
            } catch (IOException e) {
                enqueueButton.getStyleClass().add("invalid");
                System.err.println("Unable to enqueue");
                e.printStackTrace();
            }
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
        try {
            String opponentName = playerList.getSelectionModel().getSelectedItem();
            Tile.Player player = connection.selectOpponent(opponentName);
            if(opponentName != null && player != null) {
                joined(new JoinTask.Opponent(player, opponentName));
            } else {
                selectButton.getStyleClass().add("invalid");
            }
        } catch (IOException e) {
            selectButton.getStyleClass().add("invalid");
            System.err.println("Unable to select opponent");
            e.printStackTrace();
        }
    }

    public void joined(JoinTask.Opponent opponent) {
        playerRefresher.stop();
        listener.establishedGame(opponent);
    }

    private void updatePlayerList() {
        try {
            ObservableList<String> playerNames = FXCollections.observableArrayList(connection.getPlayerList());
            playerList.setItems(playerNames);
        } catch (IOException e) {
            System.err.println("Could not request player list");
        }
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
