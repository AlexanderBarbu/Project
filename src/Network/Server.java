package Network;

import Utility.Logger;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    
    private ServerSocket serverSocket = null;
    private Router router = null;

    private Map<Integer, MessageCallback> callback = new ConcurrentHashMap<>();

    /**
     * Called when a message arrives. If the message was a response
     * to a client's message, then upon returning from this function
     * then message.getCallback() will not be null.
     * 
     * @param sender The client that sent the message
     * @param data The data that was received
     */
    protected void onReceiveMessage(Socket sender, Message message) {
        int requestId = message.getRequestId();
        if (callback.containsKey(requestId)) {
            MessageCallback mcb = callback.get(requestId);
            message.setCallback(mcb);
            // WARNING: If we get multiple responses from
            // a broadcasted message, only the first response
            // will trigger the callback. Implement a solution if needed
            callback.remove(requestId);
            mcb.callback(sender, message);
        }
    }

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
                    String data;
                    while ((data = in.readLine()) != null) {
                        Message message = new Message(data);
                        onReceiveMessage(socket, message);
                        if (message.getFunctionId() == Message.REQUEST_DISCONNECT) {
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
    public void send(int clientIndex, Message message) {
        saveCallback(message);
        Logger logger = new Logger("Server");
        logger.write("Sending " + message.toString());
        router.send(clientIndex, message);
    }

    /**
     * Sends a message to the given client socket
     * 
     * @param target Target socket
     * @param data
     */
    public void send(Socket target, Message message) {
        saveCallback(message);
        Logger logger = new Logger("Server");
        logger.write("Sending " + message.toString());
        router.send(target, message);
    }

    /**
     * Sends a message to all connected sockets
     * 
     * @param data Message
     */
    public void broadcast(Message message) {
        saveCallback(message);
        router.broadcast(message);
    }

    /**
     * @return The number of clients that have connected since launching
     */
    public int getNumberOfConnections() {
        return router.getNumberOfLinks();
    }

    public void saveCallback(Message message) {
        MessageCallback mcb = message.getCallback();
        if (mcb != null) {
            callback.put(message.getRequestId(), mcb);
        }
    }

    protected Router GetRouter() {
        return this.router;
    }
}
