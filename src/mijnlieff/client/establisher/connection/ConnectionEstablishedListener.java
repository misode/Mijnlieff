package mijnlieff.client.establisher.connection;

import mijnlieff.client.Connection;

import java.io.IOException;

public interface ConnectionEstablishedListener {

    void establishedConnection(Connection connection);
}
