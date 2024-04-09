import Network.Client;
import Network.NetUtil;
import Network.MessageBuilder;
import Network.Message;
import Utility.Logger;

import java.net.*;

class AppClient extends Client {

    private String username = null;
    private String authToken = null;

    private Logger logger = new Logger("AppClient");

    public AppClient() {
        logger.write("Connecting to Master...");
        connect(NetUtil.getServerIp(), NetUtil.getMasterAppPort(), WaitMode.WAIT_UNTIL_CONNECTS);
        logger.write("Connected to Master");
    }

    public boolean login(String username, String password) {
        MessageBuilder mb = new MessageBuilder();
        mb.setFunctionID(Message.REQUEST_LOGIN);
        mb.setCallback((s, r) -> {
            int functionId = r.getFunctionId();
            if (functionId == Message.LOGIN_ACCEPTED) {
                this.username = username;
                this.authToken = r.getParams()[0];
            } else {
                this.username = null;
                this.authToken = null;
            }
            synchronized (this) {
                this.notify();
            }
        });
        mb.addParam(username);
        mb.addParam(Utility.StringHasher.Hash(password));
        sendToServer(mb.get());
        synchronized (this) {
            try {
                this.wait();
                return this.authToken != null;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public void testMapReduce() {
        logger.write("Sending a reduction test request...");
        
        MessageBuilder mb = new MessageBuilder();
        mb.setFunctionID(Message.TEST_REDUCTION);
        mb.setCallback((Socket socket, Message message) -> {
            String[] params = message.getParams();
            logger.write("Received TEST_REDUCTION results: ");
            for (String param : params) {
                System.out.println(param);
            }
        });
        mb.addParam(username);
        mb.addParam(authToken);
        sendToServer(mb.get());
        logger.write("Reduction test message sent");
    }
}
