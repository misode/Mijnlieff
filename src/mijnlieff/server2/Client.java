package mijnlieff.server2;

public class Client implements AutoCloseable {

    private Writer writer;
    private ClientHandler.State state;
    private String identifier;

    public Client(Writer writer) {
        this.writer = writer;
        this.state = null;
        this.identifier = null;
    }

    public boolean isConnected() {
        return state != null;
    }

    public void setState(ClientHandler.State state) {
        this.state = state;
    }

    public ClientHandler.State getState() {
        return state;
    }

    public void write(String... messages) {
        writer.write(messages);
    }

    public void identify(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isEnqueued() {
        return state != null && state.isEnqueued();
    }

    public void close() {
        this.state = null;
    }

}