package mijnlieff.client;

import javafx.concurrent.Task;

import java.io.BufferedReader;

public class BoardEstablisherTask extends Task<BoardSetting> {

    private BufferedReader in;

    public BoardEstablisherTask(BufferedReader in) {
        this.in = in;
    }

    @Override
    protected BoardSetting call() throws Exception {
        System.out.println("Start listening...");
        //String response = in.readLine();
        // TODO: remove temporary response
        Thread.sleep(5000);
        String response = "+ 0 0 0 2 2 0 2 2";
        return new BoardSetting(response.substring(2));
    }
}
