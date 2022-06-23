package proc.sketches;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectionManager {

    private static ConnectionManager connectionManager = null;

    private ConnectionAddress ownAddress;
    private ConnectionManagerCallback connectionManagerCallback;
    private final ArrayList<ReceivedMessageCallback> messageCallbacks;
    private final HashMap<String, SendMessageCallback> sendMessageCallbacks;

    public static ConnectionManager getInstance(ConnectionAddress ownAddress) {
        if (connectionManager == null) {
            connectionManager = new ConnectionManager(ownAddress);
        }
        return connectionManager;
    }

    public ConnectionManager(ConnectionAddress ownAddress) {
        this.ownAddress = ownAddress;
        this.connectionManagerCallback = null;
        this.messageCallbacks = new ArrayList<>();
        this.sendMessageCallbacks = new HashMap<>();
    }

    public void sendMessage(ConnectionAddress destinationAddress, String message) {
        sendMessage(destinationAddress, message, null);
    }

    public void sendMessage(ConnectionAddress destinationAddress, String message, String callbackOwner) {
        Runnable send = () -> {
            try {
                sendBytes(message, destinationAddress);
                if (callbackOwner != null) {
                    sendMessageCallbacks.get(callbackOwner).onSendSuccessful();
                }
            } catch (Exception e) {
                if (callbackOwner != null) {
                    sendMessageCallbacks.get(callbackOwner).onFailure(e);
                }
            }
        };
        send.run();
    }

    private void sendBytes(String value, ConnectionAddress destinationAddress) {
        try {
            Socket socket = new Socket(destinationAddress.getIpAddress(), destinationAddress.getPort());
            DataOutputStream sending = new DataOutputStream(socket.getOutputStream());
            byte[] bytes = value.getBytes();
            sending.write(bytes, 0, bytes.length);
            sending.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setConnectionManagerCallback(ConnectionManagerCallback connectionManagerCallback) {
        this.connectionManagerCallback = connectionManagerCallback;
    }

    public void setOwnAddress(ConnectionAddress ownAddress) {
        this.ownAddress = ownAddress;
    }

    public void addSendMessageCallback(SendMessageCallback sendMessageCallback, String callbackOwner) {
        this.sendMessageCallbacks.put(callbackOwner, sendMessageCallback);
    }

    public void addReceiveMessageCallback(ReceivedMessageCallback reciveMessageCallback) {
        this.messageCallbacks.add(reciveMessageCallback);
    }

    public ConnectionAddress getOwnAddress() {
        return ownAddress;
    }

    public void connect(ArrayList<ConnectionAddress> screensPorts) {
        try {
            connectInternal(screensPorts);
            if (connectionManagerCallback != null) {
                connectionManagerCallback.onConnectedSuccessfully();
            }
            List<String> connectedIpAddresses = screensPorts.stream().map(ConnectionAddress::getIpAddress).collect(Collectors.toList());
            for (ReceivedMessageCallback messageCallback : messageCallbacks) {
                if (connectedIpAddresses.contains(messageCallback.getAddress().getIpAddress())) {
                    getMessage(messageCallback);
                }
            }
        } catch (Exception e) {
            if (connectionManagerCallback != null) {
                connectionManagerCallback.onConnectionFailed();
            }
        }
    }

    private void connectInternal(ArrayList<ConnectionAddress> screensPorts) throws InterruptedException {

    }

    private void getMessage(ReceivedMessageCallback messageCallback) {
        Runnable listenToMessages = () -> {
            while (true) {
                String message = readMessage(messageCallback.getAddress());
                if (message != null) {
                    messageCallback.onMessageReceived(message);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        listenToMessages.run();

    }

    private String readMessage(ConnectionAddress address) {
        try {
            ServerSocket ssoc = new ServerSocket(address.getPort());
            Socket socket = ssoc.accept();
            DataInputStream reading = new DataInputStream(socket.getInputStream());
            int count = reading.available();
            byte[] message = new byte[count];
            reading.read(message);
            ssoc.close();
            socket.close();
            return new String(message);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
