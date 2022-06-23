package proc.sketches;

public interface SendMessageCallback {
    String getCallbackOwner();

    void onSendSuccessful();

    void onFailure(Exception e);
}
