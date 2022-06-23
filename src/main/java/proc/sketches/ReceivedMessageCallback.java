package proc.sketches;

public interface ReceivedMessageCallback {
    ConnectionAddress getAddress();

    void onMessageReceived(String message);
}
