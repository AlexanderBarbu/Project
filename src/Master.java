import java.net.*;

import Network.Message;
import Network.NetUtil;
import Network.Server;

public class Master {

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

        private void handleInitialMessage(Socket socket, Message message) {
            final int funcId = message.getFunctionId();
            if (funcId == Message.REQUEST_LOGIN) {
                handleLoginRequest(socket, message);
            } else if (funcId == Message.REQUEST_REGISTRATION) {
                // TODO
            } else {
                String[] params = message.getParams();
                if (params.length < 2) {
                    send(socket, new Message(message.getRequestId(), Message.INVALID_PARAMS, null));
                } else {
                    if (Authenticator.validateToken(params[0], params[1])) {
                        handleRequest(socket, message);
                    } else {
                        send(socket, new Message(message.getRequestId(), Message.INVALID_TOKEN, null));
                    }
                }
            }
        }

        private void handleLoginRequest(Socket socket, Message message) {
            String[] params = message.getParams();
            if (params.length != 2) {
                Message invalidParamsMsg = new Message(message.getRequestId(), Message.INVALID_PARAMS, null);
                send(socket, invalidParamsMsg);
            } else {
                String authToken = Authenticator.authenticate(params[0], params[1]);
                if (authToken.isEmpty()) {
                    Message wrongCredentialsMsg = new Message(
                        message.getRequestId(),
                        Message.LOGIN_REJECTED, 
                        "Username or password"
                    );
                    send(socket, wrongCredentialsMsg);
                } else {
                    Message loginSuccessMsg = new Message(
                        message.getRequestId(),
                        Message.LOGIN_ACCEPTED,
                        authToken
                    );
                    loginSuccessMsg.setCallback((s, m) -> {
                        if (m.getFunctionId() == Message.PING) {
                            Message pingMsg = new Message(
                                m.getRequestId(),
                                Message.PING,
                                null
                            );
                            send(s, pingMsg);
                        }
                    });
                    send(socket, loginSuccessMsg);
                }
            }
        }

        private void handleRequest(Socket socket, Message message) {
            send(socket, new Message(message.getRequestId(), Message.PING, "All good bruv"));
        }

    }

    private class WorkerServer extends Server {

    }

    private ClientServer clientServer = new ClientServer();
    private WorkerServer workerServer = new WorkerServer();

    public Master() {
        clientServer.start(NetUtil.getMasterAppPort());
        workerServer.start(NetUtil.getMasterWorkerPort());

        for (int i = 0; i < 5; ++i) {
            
        }
    }
}