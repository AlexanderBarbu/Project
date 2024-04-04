import java.net.*;
import java.util.HashMap;
import java.io.*;

public class Client {

    public enum WaitMode {
        TRY_ONCE,
        WAIT_UNTIL_CONNECTS
    }

    private int port = 0;
    private Socket socket = null;

    private BufferedReader in = null;
    private PrintWriter out = null;

    private HashMap<Integer, MessageCallback> callback = new HashMap<>();

    protected void onReceiveMessage(Message msg) {
        int requestId = msg.getRequestId();
        if (callback.containsKey(requestId)) {
            MessageCallback mcb = callback.get(requestId);
            callback.remove(requestId);
            mcb.callback(socket, msg);
        }
    }

    protected void onDisconnectedFromServer() {}
    protected void onConnectedToServer() {}
    protected void onConnectionFailed() {}

    /**
     * Connects to the given server IP and port
     * 
     * @param ip IP address of server
     * @param port Port to connect to
     * @param mode
     * @see WaitMode
     */
    public void connect(String ip, int port, WaitMode mode) {
        if (socket == null) {
            try {
                if (mode == WaitMode.WAIT_UNTIL_CONNECTS) {
                    tryConnectUntilSuccess(ip, port);
                } else {
                    socket = new Socket(ip, port);
                }
                this.port = port;
                onConnectedToServer();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                Thread listenerThread = new Thread(() -> receiveData());
                listenerThread.start();
            } catch (IOException e) {
                onConnectionFailed();
            }
        }
    }

    private void tryConnectUntilSuccess(String ip, int port) {
        while (socket == null) {
            try {
                socket = new Socket(ip, port);
            } catch (IOException ioe) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    // ...
                }
            }
        }
    }

    /**
     * Sends data to the server
     * 
     * @param data
     */
    public void sendToServer(Message message) {
        if (out != null) {
            MessageCallback callback = message.getCallback();
            if (callback != null) {
                this.callback.put(message.getRequestId(), callback);
            }
            out.println(message.toString());
        }
    }

    /**
     * Blocks until it receives data from the server, should be called
     * in a seperate thread
     */
    private void receiveData() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            try {
                String data;
                while (true) {
                    while ((data = in.readLine()) != null) {
                        Message message = new Message(data);
                        onReceiveMessage(message);
                    }
                }
            } catch (IOException e) {
                onDisconnectedFromServer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnects from the server and closes the socket
     */
    public void disconnect() {
        if (socket != null) {
            try {
                sendToServer(new Message(0, Message.REQUEST_DISCONNECT, null));
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPort() {
        return port;
    }
}
