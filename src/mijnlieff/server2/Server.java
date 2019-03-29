package mijnlieff.server2;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Server {

    private int portNumber;
    private ExecutorService pool;
    private ClientHandler handler;

    public Server(int portNumber) {
        this.portNumber = portNumber;
        this.pool = Executors.newCachedThreadPool();
        this.handler = new ClientHandler();
    }

    public void listen() {
        if(pool.isShutdown()) return;
        pool.execute(handler);
        System.err.println("Starting to listen for connections...");
        try(ServerSocket serverSocket = new ServerSocket(portNumber)) {
            for(;;) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(() -> handle(clientSocket));
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        System.err.println("Stopped listening for connections.");
        pool.shutdown();
    }

    private void handle(Socket socket) {
        String remote = socket.getInetAddress().toString() + " : " + socket.getPort();
        System.err.println("Accepted connection from " + remote);
        try(
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Writer writer = new Writer(new PrintWriter(socket.getOutputStream(), true));
                Client client = handler.register(writer)
        ) {
            pool.execute(writer);
            String inputLine;
            while(client.isConnected() && (inputLine = in.readLine()) != null) {
                if(client.isConnected()) handler.handle(client, inputLine);
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        System.err.println("Closed connection to " + remote);
    }

    public static void main(String... args) {
        new Server(
                Integer.parseInt(args[0])
        ).listen();
    }

}