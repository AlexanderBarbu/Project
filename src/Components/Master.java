package Components;
import java.net.*;

import Authentication.Authenticator;
import Model.Hotel;
import Model.Room;
import Network.DynamicRoutingPolicy;
import Network.Message;
import Network.MessageBuilder;
import Network.NetUtil;
import Network.Server;
import Utility.Logger;

public class Master {

    private Logger logger = new Logger("Master");

    private class ClientServer extends Server {

        @Override
        protected void onReceiveMessage(Socket socket, Message message) {
            super.onReceiveMessage(socket, message);
            // If the callback is not null after returning from
            // super.onReceiveMessage, then the message was a response
            // to one of the server's messages
            if (message.getCallback() == null) {
                handleInitialMessage(socket, message);
            }
        }

        /**
         * Handles an 'initial' message, meaning a message that didn't
         * arrive as a response to a message the server sent
         * 
         * @param socket Socket that sent the data
         * @param message The message
         */
        private void handleInitialMessage(Socket socket, Message message) {
            switch (message.getFunctionId()) {
                case Message.REQUEST_LOGIN:
                    logger.write("Received login request");
                    handleLoginRequest(socket, message);
                    break;
                case Message.REQUEST_REGISTRATION:
                    logger.write("Received registration request");
                    break;
                default:
                    String[] params = message.getParams();
                    // Requests sent to the Master from the App must include a username
                    // and the authentication token, in order to ensure that the user
                    // has access to the requested service
                    if (params.length < 2) {
                        sendInvalidParameterMessage(socket, message.getRequestId());
                    } else {
                        if (Authenticator.validateToken(params[0], params[1])) {
                            handleRequest(socket, message);
                        } else {
                            send(socket, new Message(message.getRequestId(), Message.INVALID_TOKEN, null));
                        }
                    }
            }
        }

        /**
         * Checks if the given credentials are valid, and if so,
         * accepts the login and sends back an authentication token.
         * Otherwise, a rejection message is sent.
         * 
         * @param socket Socket that sent the login request
         * @param message The message the socket sent
         */
        private void handleLoginRequest(Socket socket, Message message) {
            String[] params = message.getParams();
            String authToken = Authenticator.authenticate(params[0], params[1]);
            if (authToken.isEmpty()) {
                sendLoginRejectedMessage(socket, message.getRequestId());
            } else {
                Message loginSuccessMsg = new Message(
                    message.getRequestId(),
                    Message.LOGIN_ACCEPTED,
                    authToken
                );
                send(socket, loginSuccessMsg);
            }
        }

        /**
         * Sends an INVALID_PARAMS message upon not receiving enough parameters
         * or receiving invalid ones
         * 
         * @param socket The socket that sent the message
         * @param requestId The request id of the forementioned message
         */
        private void sendInvalidParameterMessage(Socket socket, int requestId) {
            send(socket, new Message(requestId, Message.INVALID_PARAMS, null));
        }

        /**
         * Sends a LOGIN_REJECTED message back to the application
         * upon receiving the wrong credentials
         * 
         * @param socket The socket that sent the message
         * @param requestId The request id of the forementioned message
         */
        private void sendLoginRejectedMessage(Socket socket, int requestId) {
            Message loginRejectedMsg = new Message(
                requestId,
                Message.LOGIN_REJECTED, 
                "Username or password"
            );
            send(socket, loginRejectedMsg);
        }

        private void handleRequest(Socket socket, Message message) {
            logger.write("Handling request...");
            logger.write(message.toString());
            switch (message.getFunctionId()) {
                case Message.SAVE_HOTEL:
                    saveHotel(message);
                    break;
                case Message.SAVE_ROOM:
                    saveRoom(message);
                    break;
                case Message.ADD_DATES:
                    addReservationDates(message);
                    break;
                case Message.RESERVE_ROOM:
                    reserveRoom(message);
                    break;
                case Message.GET_OWNED_HOTELS:
                    initMapReduceProcess(socket, Message.GET_OWNED_HOTELS, message.getRequestId(), null);
                    break;
                case Message.FILTER_HOTELS:
                    initMapReduceProcess(socket, Message.FILTER_HOTELS, message.getRequestId(), new String[] { message.getParams()[2] } );
                    break;

                case Message.GET_RESERVATIONS_PER_AREA:
                    initMapReduceProcess(socket, Message.GET_RESERVATIONS_PER_AREA, message.getRequestId(), new String[] { message.getParams()[2] });
                    break;
            }
        }

    }

    private class WorkerServer extends Server {}
    private class ReducerServer extends Server {}

    private ClientServer clientServer = new ClientServer();
    private WorkerServer workerServer = new WorkerServer();
    private ReducerServer reducerServer = new ReducerServer();

    public Master() {
        clientServer.start(NetUtil.getMasterAppPort());
        workerServer.start(NetUtil.getMasterWorkerPort());  
        workerServer.GetRouter().setRoutingPolicy(new DynamicRoutingPolicy());      
        reducerServer.start(NetUtil.getMasterReducerPort());
    }

    private void initMapReduceProcess(Socket client, int functionId, int requestId, String[] args) {
        MessageBuilder mb = new MessageBuilder();
        // We'll let the server know that we're going to
        // start a map reduce process
        mb.setFunctionID(Message.INIT_REDUCTION);
        mb.setRequestId(requestId);
        mb.setCallback((Socket socket, Message message) -> {
            logger.write("Received READY_FOR_REDUCTION.");
            // Once we get here, the reducer is expecting map results
            // from the workers with a requestId equal to the INIT_REDUCTION
            // message, so we'll send the workers a message with that
            // requestId. The workers will then send the answer with that reqId
            MessageBuilder mb2 = new MessageBuilder();
            mb2.setFunctionID(functionId);
            mb2.setRequestId(message.getRequestId());
            mb2.setCallback((s, r) -> returnMapReduceResults(client, r));
            
            if (args != null) {
                for (String arg : args) {
                    mb2.addParam(arg);
                }
            }

            Message reductionMsg = mb2.get();
            // The message is sent through the worker server,
            // but is received through the reducer server, so
            // we have to save the callback there in order for
            // it to be called
            this.reducerServer.saveCallback(reductionMsg);
            // We'll remove the callback so it won't be saved in
            // the worker server, in order to save memory
            reductionMsg.setCallback(null);
            logger.write("Broadcasting to workers");
            this.workerServer.broadcast(reductionMsg);
        });
        // The INIT_REDUCTION message takes the number of workers as the only argument
        mb.addParam(Integer.toString(workerServer.getNumberOfConnections()));
        // clientIndex = 0 because the only client is the reducer
        reducerServer.send(0, mb.get());
    }

    /**
     * Sends the map reduce results back to the app
     * 
     * @param socket 
     * @param response
     */
    private void returnMapReduceResults(Socket client, Message response) {
        logger.write("Returning data to client");
        clientServer.send(client, response);
    }

    private void saveHotel(Message message) {
        int connections = workerServer.getNumberOfConnections();
        if (connections > 0) {
            String[] params = message.getParams();
            Hotel hotel = new Hotel(params[2]);

            int index = Math.abs(hotel.Name.hashCode()) % connections;
            workerServer.send(index, message);
        }
    }

    private void saveRoom(Message message) {
        int connections = workerServer.getNumberOfConnections();
        if (connections > 0) {
            String[] params = message.getParams();
            Room room = new Room(params[2]);

            int index = Math.abs(room.getHotelName().hashCode()) % connections;
            workerServer.send(index, message);
        }
    }

    private void reserveRoom(Message message) {
        int connections = workerServer.getNumberOfConnections();
        if (connections > 0) {
            String hotelName = message.getParams()[2];

            int index = Math.abs(hotelName.hashCode()) % connections;
            logger.write("Index=" + index);
            workerServer.send(index, message);
        }
    }

    private void addReservationDates(Message message) {
        int connections = workerServer.getNumberOfConnections();
        if (connections > 0) {
            String hotelName = message.getParams()[2];

            int index = Math.abs(hotelName.hashCode()) % connections;
            logger.write("Index=" + index);
            workerServer.send(index, message);
        }
    }
}