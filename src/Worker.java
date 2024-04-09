import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import Network.Client;
import Network.Hybrid;
import Network.Message;
import Network.MessageBuilder;
import Network.NetUtil;
import Network.Client.WaitMode;

import Utility.Logger;

public class Worker extends Hybrid {

    /**
     * 
     */
    private class WorkerClient extends Client {}
    private class ReducerClient extends Client {}

    private int id;
    private ReducerClient reducerClient = new ReducerClient();

    /**
     * We want to connect to multiple leaders
     * The key is the ID of the leader
     * The value is the client which is connected to the leader
     */
    private HashMap<Integer, WorkerClient> clients = new HashMap<>();

    /**
     * Used to store the worker's own data as well as the replicated
     * data of the leader worker. The hashmap matches the id of the
     * worker to its data
     */
    private HashMap<Integer, Object> workerData = new HashMap<>();

    private Logger logger = null;

    public Worker(int id) {
        logger = new Logger("Worker " + id);
        this.id = id;
        logger.write("Starting server...");
        startServer(NetUtil.idToPort(id));
        logger.write("Server started");
        logger.write("Connecting to master...");
        connectToServer(NetUtil.getServerIp(), NetUtil.getMasterWorkerPort());
        logger.write("Connected to master");
        logger.write("Connecting to reducer");
        reducerClient.connect(NetUtil.getServerIp(), NetUtil.getReducerPort(), WaitMode.WAIT_UNTIL_CONNECTS);
        logger.write("Connected to reducer");
    }

    public int getId() {
        return this.id;
    }

    public Object getDataCopy() {
        return null;
    }

    /**
     * Establishes a connection with the given worker and replicates its data
     * 
     * @param leader 
     * @see WorkerClient
     */
    public void followWorker(Worker leader) {
        if (leader == null) {
            return;
        }

        final int leaderId = leader.getId();

        if (!workerData.containsKey(leader.getId())) {
            WorkerClient client = new WorkerClient();
            client.connect("127.0.0.1", NetUtil.idToPort(leaderId), Client.WaitMode.WAIT_UNTIL_CONNECTS);
            clients.put(leaderId, client);
            workerData.put(leaderId, leader.getDataCopy());
        }
    }

    private void processMessageFromServer(int port, Message message) {
        switch (message.getFunctionId()) {
            case Message.TEST_REDUCTION:
                logger.write("Processing for " + message.getParams()[0]);
                MessageBuilder mb = new MessageBuilder();
                mb.setFunctionID(message.getFunctionId());
                mb.setRequestId(message.getRequestId());
                for (int i = 1; i <= 5; ++i) {
                    mb.addParam("area" + i + ":" + ThreadLocalRandom.current().nextInt(0, 100));
                }
                logger.write("Sending response to reducer");
                reducerClient.sendToServer(mb.get());
                break;
        }
    }

    private void sendToFollowers(Message message) {
        broadcast(message);
    }

    /**
     * Called when the worker receives a message from the Master
     * 
     * @param data Data to be sent
     */
    @Override
    protected void onReceiveMessageFromServer(Message message) {
        logger.write("Received message from master");
        processMessageFromServer(NetUtil.getMasterWorkerPort(), message);
    }

    /**
     * Sends a data to either the master or one of the connected workers
     * 
     * @param port Port of the server, identifying the destination server
     * @param data Data to be sent
     */
    private void sendToServer(int port, Message message) {
        if (port == NetUtil.getMasterWorkerPort()) {
            sendToServer(message);
        } else if (clients.containsKey(port)) {
            clients.get(port).sendToServer(message);
        }
    }

    public void scheduleDeath(int millliseconds) {
        Thread thread = new Thread(() -> {
            try { Thread.sleep(millliseconds); } catch (InterruptedException ie) {}
            this.disconnectFromServer();
        });
        thread.start();
    }
}