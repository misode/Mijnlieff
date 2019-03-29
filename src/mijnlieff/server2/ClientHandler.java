package mijnlieff.server2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Arrays;

public class ClientHandler implements Runnable {

    private static final Random RG = new Random();

    private BlockingQueue<Command> queue;
    private Map<String, Client> clients;
    private boolean stopped;

    public ClientHandler() {
        this.queue = new LinkedBlockingQueue<>();
        this.clients = new HashMap<>();
        this.stopped = false;
    }

    public Client register(Writer writer) {
        Client client = new Client(this, writer);
        client.setState(new Begin(client));
        return client;
    }

    public void remove(Client client) {
        client.getState().quit();
        if(client.getIdentifier() != null) {
            clients.remove(client.getIdentifier());
        }
    }

    public void handle(Client client, String line) {
        try {
            queue.put(new Command(client, line));
        } catch(InterruptedException e) {}
    }

    private static class Command {
        public Client client;
        public String line;
        public Command(Client client, String line) {
            this.client = client;
            this.line = line;
        }
    }

    public void run() {
        Command command = null;
        try {
            while(!stopped && (command = queue.take()) != null) {
                command.client.setState(realHandle(command.client, command.line));
            }
        } catch(InterruptedException e) {}
        stopped = true;
    }

    private State realHandle(Client client, String line) {
        String[] split = line.split(" ");
        switch(split[0]) {
            case "Q":
                return client.getState().quit();
            case "W":
                return client.getState().getQueue();
            case "I":
                return client.getState().identify(line);
            case "P":
                return client.getState().enqueue();
            case "R":
                return client.getState().retract();
            case "C":
                return client.getState().choose(line);
            case "X":
                return client.getState().forward(line);
            default:
                return client.getState().error();
        }
    }

    public interface State {
        State quit();
        State getQueue();
        State identify(String line);
        State enqueue();
        State retract();
        State choose(String line);
        State forward(String line);
        State error();

        boolean isClosed();
        boolean isEnqueued();
    }

    public class Closed implements State {
        /* if we ignore them they might leave */
        public State quit()                { return this; }
        public State getQueue()            { return this; }
        public State identify(String line) { return this; }
        public State enqueue()             { return this; }
        public State retract()             { return this; }
        public State choose(String line)   { return this; }
        public State forward(String line)  { return this; }
        public State error()               { return this; }

        public boolean isClosed()   { return true; }
        public boolean isEnqueued() { return false; }
    }

    public abstract class Open implements State {
        protected final Client client;

        public Open(Client client) {
            this.client = client;
        }

        public State quit() {
            if(client.getIdentifier() != null) clients.remove(client.getIdentifier());
            return new Closed();
        }

        public State getQueue() {
            for(Client other : clients.values()) {
                if(other.isEnqueued()) {
                    client.write("+ " + other.getIdentifier());
                }
            }
            client.write("+");
            return this;
        }

        public State identify(String line) { return error(); }
        public State enqueue()             { return error(); }
        public State retract()             { return error(); }
        public State choose(String line)   { return error(); }
        public State forward(String line)  { return error(); }

        public State error() {
            client.write("?");
            return quit();
        }

        public boolean isClosed() { return false; }
        public boolean isEnqueued() { return false; }

        protected String substring(String string, int begin) {
            if(begin > string.length()) {
                return "";
            } else {
                return string.substring(begin);
            }
        }
    }

    public class Begin extends Open {
        public Begin(Client client) {
            super(client);
        }

        public State identify(String line) {
            String identifier = substring(line, 2);
            if(clients.containsKey(identifier)) {
                client.write("-");
                return this;
            } else {
                client.identify(identifier);
                clients.put(identifier, client);
                client.write("+");
                return new Identified(client);
            }
        }

        public State forward(String input) {
            return new PartOne(client, new LinkedList<>(Arrays.asList("X F 2 2 o", "X F 2 3 +", "X T 3 3 X"))).forward(input);
        }
    }

    public class PartOne extends Open {
        private LinkedList<String> movesLeft;

        public PartOne(Client client, LinkedList<String> movesLeft) {
            super(client);
            this.movesLeft = movesLeft;
        }

        public State forward(String input) {
            client.write(movesLeft.poll());
            if(movesLeft.isEmpty()) {
                return quit();
            } else {
                return this;
            }
        }
    }

    public class Identified extends Open {
        public Identified(Client client) {
            super(client);
        }

        public State enqueue() {
            return new Enqueued(client);
        }

        public State choose(String line) {
            String identifier = substring(line, 2);
            Client other = clients.get(identifier);
            if(other == null || !other.isEnqueued()) {
                client.write("-");
                return this;
            } else {
                if(RG.nextBoolean()) {
                    client.write("+ T " + other.getIdentifier());
                    other.write("+ F " + client.getIdentifier());
                    other.setState(new NotTurn(other, client));
                    return new AtTurn(client, other);
                } else {
                    client.write("+ F " + other.getIdentifier());
                    other.write("+ T " + client.getIdentifier());
                    other.setState(new AtTurn(other, client));
                    return new NotTurn(client, other);
                }
            }
        }
    }

    public class Enqueued extends Open {
        public Enqueued(Client client) {
            super(client);
        }

        public boolean isEnqueued() {
            return true;
        }

        public State retract() {
            client.write("+");
            return new Identified(client);
        }
    }

    public class AtTurn extends Open {
        private Client other;
        public AtTurn(Client client, Client other) {
            super(client);
            this.other = other;
            clients.remove(client.getIdentifier());
        }

        public State quit() {
            other.quit();
            return super.quit();
        }

        public State forward(String input) {
            other.write(input);
            other.setState(new AtTurn(other, client));
            return new NotTurn(client, other);
        }
    }

    public class NotTurn extends Open {
        private Client other;
        public NotTurn(Client client, Client other) {
            super(client);
            this.other = other;
            clients.remove(client.getIdentifier());
        }

        public State quit() {
            other.quit();
            return super.quit();
        }
    }
}