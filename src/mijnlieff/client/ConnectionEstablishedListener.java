package mijnlieff.client;

import java.io.IOException;

public interface ConnectionEstablishedListener {

    void establishedConnection(Connection connection, String username) throws IOException;
}
