package mijnlieff.server;

import java.net.ServerSocket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReplayServer {

    private int portNumber;

    public ReplayServer(int portNumber) {
        this.portNumber = portNumber;
    }

    public void listen() {
        ExecutorService pool = Executors.newCachedThreadPool();
        try(ServerSocket serverSocket = new ServerSocket(portNumber)) {
            for (;;) pool.execute(new ReplayHandler(serverSocket.accept()));
        } catch (IOException e) {
            /* Als het openen van sockets faalt, kunnen we toch niets nuttig
             * doen. We printen gewoon de foutboodschap helemaal af om
             * zoveel mogelijk informatie te geven over de fout. */
            e.printStackTrace();
        }
        pool.shutdown();
    }

    public static void main(String... args) {
        new ReplayServer(
                Integer.parseInt(args[0])
        ).listen();
    }

}