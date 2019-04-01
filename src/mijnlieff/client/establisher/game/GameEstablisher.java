package mijnlieff.client.establisher.game;

import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mijnlieff.client.Connection;
import mijnlieff.client.ConnectionListener;
import mijnlieff.client.game.Player;

import java.util.ArrayList;

/**
 * Companion class for the player list.
 * Used by Mijnlieff to find two players for a game
 */
public class GameEstablisher extends ConnectionListener {

    private VBox view;

    private ListView<String> playerList;
    private Button enqueueButton;
    private Button selectButton;

    private GameEstablishedListener listener;
    private boolean enqueued;

    public GameEstablisher(Connection connection, GameEstablishedListener listener) {
        super(connection);
        initialize();
        this.listener = listener;
    }

    private void initialize() {
        playerList = new ListView<>();
        playerList.setCellFactory(l -> new PlayerCell());

        enqueueButton = new Button("Enqueue");
        enqueueButton.setOnAction(e -> doQueue());

        selectButton = new Button("Select");
        selectButton.setOnAction(e -> doSelect());

        view = new VBox(playerList, new HBox(enqueueButton, selectButton));
        view.getStylesheets().add("mijnlieff/client/style.css");

    }

    public Parent asParent() {
        return view;
    }

    private void doQueue() {
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

    private void doSelect() {
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
