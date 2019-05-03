package mijnlieff.client.establisher.connection;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mijnlieff.client.Connection;
import mijnlieff.client.ConnectionListener;

import java.io.IOException;

/**
 * Companion class for the connection initialization stage
 */
public class ConnectionEstablisher extends ConnectionListener {

    private VBox view;

    private TextField hostName;
    private TextField portNumber;
    private TextField username;
    private Label connecting;

    private ConnectionEstablishedListener listener;

    public ConnectionEstablisher(ConnectionEstablishedListener listener) {
        super(new Connection());
        initialize();
        this.listener = listener;
    }

    private void initialize() {
        hostName = new TextField("cercan.ugent.be");
        hostName.setPromptText("server host");
        hostName.setPrefWidth(10000);

        portNumber = new TextField("80");
        portNumber.setPromptText("port number");
        portNumber.setMinWidth(90);

        username = new TextField();
        username.setPromptText("username");

        Button joinButton = new Button("Join");
        joinButton.setOnAction(e -> this.doJoin());

        connecting = new Label("Connecting...");
        connecting.setPrefHeight(26);
        connecting.setVisible(false);

        view = new VBox(new HBox(hostName, portNumber), username, new HBox(joinButton, connecting));
        view.getStyleClass().add("root");
        view.getStylesheets().add("mijnlieff/client/style.css");

    }

    public Parent asParent() {
        return view;
    }

    private void doJoin() {
        if (!checkValidInput()) return;

        new Thread(this::connect).start();
        connecting.setVisible(true);
    }

    private boolean checkValidInput() {
        boolean valid = true;
        hostName.getStyleClass().removeAll("invalid");
        portNumber.getStyleClass().removeAll("invalid");
        username.getStyleClass().removeAll("invalid");
        if (hostName.getText().length() < 1) {
            hostName.getStyleClass().add("invalid");
            valid = false;
        }
        if (portNumber.getText().length() < 1) {
            portNumber.getStyleClass().add("invalid");
            valid = false;
        }
        if (!validUsername(username.getText())) {
            username.getStyleClass().add("invalid");
            valid = false;
        }
        return valid;
    }

    private boolean validUsername(String username) {
        return username.matches("[A-Za-z0-9_]+");
    }

    @Override
    public void identified(boolean success) {
        if (success) {
            listener.establishedConnection(connection);
        } else {
            username.getStyleClass().add("invalid");
        }
    }

    private void connect() {
        try {
            int port = Integer.parseInt(portNumber.getText());
            connection.start(hostName.getText(), port);
            connection.identify(username.getText());
        } catch (NumberFormatException e) {
            portNumber.getStyleClass().add("invalid");
        } catch (IOException e) {
            hostName.getStyleClass().add("invalid");
            portNumber.getStyleClass().add("invalid");
        }
        connecting.setVisible(false);
    }
}
