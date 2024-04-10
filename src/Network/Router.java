package Network;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intermediate stage for sending messages to sockets.
 * Can be used by servers in cooperation with a routing policy in order
 * to ensure that a message reacheas a living client
 * @see Server
 * @see Client
 * @see IRoutingPolicy
 */
public class Router {

    /**
     * Implements the default routing policy
     * @see IRoutingPolicy
     */
    private class DefaultRoutingPolicy implements IRoutingPolicy {
        
        @Override
        public List<Integer> GetAlternativeRouteIndexes(int index, int numberOfNodes) {
            //return new ArrayList<>();
            ArrayList<Integer> result = new ArrayList<>();
            result.add(new Integer((index + 1) % numberOfNodes));
            return result;
        }

    }

    private Map<Socket, Boolean> isConnected = new ConcurrentHashMap<>();
    private List<Socket> linkedSockets = new ArrayList<>();
    private IRoutingPolicy routingPolicy = new DefaultRoutingPolicy();

    public void setRoutingPolicy(IRoutingPolicy routingPolicy) {
        if (routingPolicy != null) {
            this.routingPolicy = routingPolicy;
        }
    }

    /**
     * Links a socket to the router.
     * If there are N sockets linked to the router before calling this function,
     * then the passed socket gets the index N (first socket has index 0)
     * 
     * @param socket 
     */
    public void linkDestination(Socket socket) {
        if (!isConnected.containsKey(socket)) {
            linkedSockets.add(socket);
            isConnected.put(socket, true);
        }
    }

    /**
     * @return The number of linked sockets, including both dead and living connections
     */
    public int getNumberOfLinks() {
        return linkedSockets.size();
    }

    /**
     * @return The number of living connections between the server and the linked sockets
     */
    public int getNumberOfConnections() {
        int activeConnections = 0;
        for (Socket linkedSocket : linkedSockets) {
            if (isConnected.get(linkedSocket)) {
                ++activeConnections;
            }
        }
        return activeConnections;
    }

    /**
     * 
     * @param socket
     * @return Whether the connection with the server and the socket is alive
     */
    private boolean isSocketConnected(Socket socket) {
        return isConnected.containsKey(socket) && isConnected.get(socket);
    }

    /**
     * Sends a message to all the sockets, and reroutes the messages
     * that would be sent to the dead ones
     * @param data
     */
    public void broadcast(Message message) {
        for (int i = 0; i < linkedSockets.size(); ++i) {
            send(linkedSockets.get(i), createMessageWithId(message, i));
        }
    }

    private Message createMessageWithId(Message message, int id) {
        MessageBuilder mb = new MessageBuilder();
        mb.setFunctionID(message.getFunctionId());
        mb.setRequestId(message.getRequestId());
        mb.addParam(Integer.toString(id));

        for (String param : message.getParams()) {
            mb.addParam(param);
        }

        return mb.get();
    }

    /**
     * Returns the index in the group of the given socket
     * 
     * @param socket
     * @return Zero indexed
     */
    private int getSocketIndex(Socket socket) {
        int index = -1;
        for (int i = 0; i < linkedSockets.size(); ++i) {
            if (linkedSockets.get(i) == socket) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * Sends data to the socket in the group with the given index
     * 
     * @param index Index of socket in the group
     * @param data Data to be transmitted
     */
    public void send(int index, Message message) {
        if (index < 0 || index >= linkedSockets.size() || message == null) {
            return;
        }
        Socket socketAtIndex = linkedSockets.get(index);
        Socket targetSocket = null;
        if (isSocketConnected(socketAtIndex)) {
            targetSocket = socketAtIndex;    
        } else {
            targetSocket = getAlternativeSocket(index);
        }
        if (targetSocket != null) {
            try {
                PrintWriter out = new PrintWriter(targetSocket.getOutputStream(), true);
                out.println(message.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends data to the given socket, iff the socket is linked to the router
     * 
     * @param socket
     * @param data
     */
    public void send(Socket socket, Message message) {
        send(getSocketIndex(socket), message);
    }

    /**
     * Gets the first available socket that can be used alternatively,
     * instead of the socket with the given index
     * 
     * @param index Index of the socket in the gorup
     */
    private Socket getAlternativeSocket(int index) {
        if (getNumberOfConnections() == 0) {
            return null;
        }

        List<Integer> altIndexes = routingPolicy.GetAlternativeRouteIndexes(
            index,
            linkedSockets.size()
        );

        // In case the policy is incorrect
        if (!altIndexes.isEmpty()) {
            // Find the first alternative socket which is connected
            for (int i = 0; i < altIndexes.size(); ++i) {
                Socket altSocket = linkedSockets.get(altIndexes.get(i));
                // Since the socket is connected and 'this' is disconnected
                // we don't need to check if they're the same
                if (isSocketConnected(altSocket)) {
                    return altSocket;
                }
            }
        }

        return null;
    }

    private Socket getAlternativeSocket(Socket socket) {
        return getAlternativeSocket(getSocketIndex(socket));
    }

    /**
     * Should be called when the server gets the onClientDisconnected message.
     * Marks the given socket as disconnected, meaning that if a message is
     * sent to the socket through the router, it will be rerouted
     * 
     * @param target Disconnected socket in the group
     * @see Server.onClientDisconnected
     */
    public void markAsDisconnected(Socket target) {
        if (isConnected.containsKey(target) && isConnected.get(target)) {
            isConnected.put(target, false);
        }
    }

}
