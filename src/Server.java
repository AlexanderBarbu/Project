import java.net.*;
import java.io.*;

public class Server {
    
    private ServerSocket serverSocket = null;
    private Router router = null;

    /**
     * Called when a message arrives
     * 
     * @param sender The client that sent the message
     * @param data The data that was received
     */
    protected void onReceiveMessage(Socket sender, String data) {}

    /**
     * Called when a client disconnects
     * 
     * @param sender The client that disconnected
     */
    protected void onClientDisconnected(Socket sender) {}

    /**
     * Called when a client connects
     * 
     * @param sender The client that connected
     */
    protected void onClientConnected(Socket sender) {}

    /**
     * Starts a server that listens for connections at the given port
     * 
     * @param port 
     */
    public void start(int port) {
        if (serverSocket == null) {
            try {
                router = new Router();
                serverSocket = new ServerSocket(port);
                Thread connectionListenerThread = new Thread(() -> listen());
                connectionListenerThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Blocks and waits for an incoming connection.
     * Once a connection comes, a thread is launched that listens for messages.
     */
    private void listen() {
        while (true) {
            try {
                Socket incomingConnectionSocket = serverSocket.accept();
                onClientConnected(incomingConnectionSocket);
                router.linkDestination(incomingConnectionSocket);

                Thread clientReceiverThread = new Thread(() -> handleConnection(incomingConnectionSocket));
                clientReceiverThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Listens for messages incoming from the given socket
     * When a message arrives, onReiceiveMessage is called
     * 
     * @param socket 
     */
    private void handleConnection(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            try {
                while (true) {
                    String message;
                    while ((message = in.readLine()) != null) {
                        onReceiveMessage(socket, message);
                        if (message.equals("req disc")) {
                            throw new IOException();
                        }
                    }
                }
            } catch (IOException e) {
                onClientDisconnected(socket);
                router.markAsDisconnected(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the nth client socket, where n = clientIndex
     * 
     * @param clientIndex Zero indexed
     * @param data
     */
    public void send(int clientIndex, String data) {
        router.send(clientIndex, data);
    }

    /**
     * Sends a message to the given client socket
     * 
     * @param target Target socket
     * @param data
     */
    public void send(Socket target, String data) {
        router.send(target, data);
    }

    /**
     * Sends a message to all connected sockets
     * 
     * @param data Message
     */
    public void broadcast(String data) {
        router.broadcast(data);
    }

    /**
     * @return The number of clients that have connected since launching
     */
    public int getNumberOfConnections() {
        return router.getNumberOfLinks();
    }

    protected Router GetRouter() {
        return this.router;
    }
}
