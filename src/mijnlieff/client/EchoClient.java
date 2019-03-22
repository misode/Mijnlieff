package mijnlieff.client;

import java.io.*;
import java.net.*;

public class EchoClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public EchoClient(String hostName, int portNumber) throws IOException {
        socket = new Socket(hostName, portNumber);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String next() throws IOException {
        out.println("X");
        return in.readLine();
    }

    public void stop() throws IOException {
        socket.close();
        out.close();
        in.close();
    }
}