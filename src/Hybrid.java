import java.net.*;


/**
 * Entity that can work both as a server and as a client at the same time
 */
public class Hybrid {

    /**
     * Client implementation that exists only to call the 
     * matching Hybrid's callback functions
     */
    private class HybridClient extends Client {
        @Override
        public void onReceiveMessage(String data) {
            Hybrid.this.onReceiveMessageFromServer(data);
        }

        @Override
        public void onConnectedToServer() {
            Hybrid.this.onConnectedToServer();
        }

        @Override
        protected void onConnectionFailed() {
            Hybrid.this.onConnectionToServerFailed();
        }

        @Override
        protected void onDisconnectedFromServer() {
            Hybrid.this.onDisconnectedFromServer();
        }
    }

    /**
     * Server implementation that exists only to call the 
     * matching Hybrid's callback functions
     */
    private class HybridServer extends Server {
        @Override
        protected void onReceiveMessage(Socket sender, String data) {
            Hybrid.this.onReceiveMessageFromClient(sender, data);
        }

        @Override
        protected void onClientDisconnected(Socket sender) {
            Hybrid.this.onClientDisconnected(sender);
        }

        @Override
        protected void onClientConnected(Socket sender) {
            Hybrid.this.onClientConnected(sender);
        }
    }

    private HybridClient client = null;
    private HybridServer server = null;

    public void startServer(int port) {
        if (server != null) {
            return;
        }
        server = new HybridServer();
        server.start(port);
    }

    public void connectToServer(String ip, int port) {
        if (client != null) {
            client.disconnect();
        }
        client = new HybridClient();
        client.connect(ip, port, Client.WaitMode.WAIT_UNTIL_CONNECTS);
    }

    public void sendToClient(Socket socket, String data) {
        if (server != null) {
            server.send(socket, data);
        }       
    }

    public void sendToClient(int clientIndex, String data) {
        if (server != null) {
            server.send(clientIndex, data);
        }
    }

    public void sendToServer(String data) {
        if (client != null) {
            client.sendToServer(data);
        }
    }

    public void broadcast(String data) {
        if (server != null) {
            server.broadcast(data);
        }
    }

    public void disconnectFromServer() {
        if (client != null) {
            client.disconnect();
        }
    }

    protected void onReceiveMessageFromServer(String data) {}
    protected void onReceiveMessageFromClient(Socket socket, String data) {}

    protected void onClientDisconnected(Socket socket) {}
    protected void onClientConnected(Socket socket) {}

    protected void onConnectionToServerFailed() {}
    protected void onConnectedToServer() {}
    protected void onDisconnectedFromServer() {}
}