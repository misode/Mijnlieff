package mijnlieff.server2;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Writer implements Runnable, AutoCloseable {

    private PrintWriter writer;
    private BlockingQueue<String> queue;
    private boolean stopped;

    public Writer(PrintWriter writer) {
        this.writer = writer;
        this.queue = new LinkedBlockingQueue<>();
        this.stopped = false;
    }

    public void run() {
        String message = null;
        try {
            while (!stopped && (message = queue.take()) != null) {
                if (!stopped) writer.println(message);
            }
        } catch (InterruptedException e) {
            /* code falls through to "stopped" should we be interrupted */
        }
        stopped = true;
    }

    public void write(String... messages) {
        try {
            for (String message : messages) {
                queue.put(message);
            }
        } catch (InterruptedException e) {}
    }

    public void close() {
        stopped = true;
        try {
            queue.put(""); // anything to unblock
        } catch (InterruptedException e) {}
    }

}