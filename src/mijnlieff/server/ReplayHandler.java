package mijnlieff.server;

import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Queue;

public class ReplayHandler implements Runnable {

    private Socket socket;

    public ReplayHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try(
                Socket local = socket; /* closes the socket */
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            Queue<String> replies = getReplies();
            String messageFromClient;
            int nextReply = 0;
            while (!replies.isEmpty() && (messageFromClient = in.readLine()) != null) {
                if (!"X".equals(messageFromClient)) {
                    /* invalid request from client: send error as last reply */
                    out.println("?");
                    replies.clear();
                } else {
                    out.println(replies.poll());
                }
            }
        } catch (IOException e) {
            /* Opnieuw niet veel dat we kunnen doen, behalve deze connectie
             * afsluiten en de error tonen. */
            e.printStackTrace();
        }
    }

    public Queue<String> getReplies() {
        /* voeg hier een testgeval in */
        return new LinkedList<>(Arrays.asList(
                "X F 2 2 o",
                "X F 2 3 +",
                "X F 3 3 X",
                "X F 1 1 @",
                "X F 3 0 +",
                "X F 0 0 X",
                "X F 3 1 o",
                "X F 2 1 X",
                "X T 3 2 o"
        ));
    }

}