import java.net.*;
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

    protected void onReceiveMessage(String data) {}
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
    public void sendToServer(String data) {
        if (out != null) {
            out.println(data);
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
                String message;
                while (true) {
                    while ((message = in.readLine()) != null) {
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
                sendToServer("req disc");
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
