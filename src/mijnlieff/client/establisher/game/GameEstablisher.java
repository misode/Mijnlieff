package mijnlieff.client.establisher.game;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import mijnlieff.client.Connection;
import mijnlieff.client.ConnectionListener;
import mijnlieff.client.game.Player;

import java.util.ArrayList;

/**
 * Companion class for the player list.
 * Used by Mijnlieff to find two players for a game
 */
public class GameEstablisher extends ConnectionListener {
    
    public ListView<String> playerList;
    public Button enqueueButton;
    public Button selectButton;

    private GameEstablishedListener listener;
    private boolean enqueued;

    public GameEstablisher() {
        enqueued = false;
    }

    public void initialize() {
        playerList.setCellFactory(l -> new PlayerCell());
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        this.connection.setListener(this);
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
        if(opponentName.equals(connection.getPlayer().getUsername())) return;
        connection.selectOpponent(opponentName);
    }

    @Override
    public void updatePlayerList(ArrayList<String> playerNames) {
        playerList.setItems(FXCollections.observableArrayList(playerNames));
    }

    @Override
    public void gameEstablished(Player opponent) {
        listener.establishedGame(opponent);
    }

    public static class PlayerCell extends ListCell<String> {

        public PlayerCell() {
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
