import java.util.*;
import java.net.*;

public class Worker extends Hybrid {

    /**
     * 
     */
    private class WorkerClient extends Client {

        @Override
        protected void onReceiveMessage(String data) {
            Worker.this.processMessageFromServer(getPort(), data);
        }

    }

    private int id;

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

    public Worker(int id) {
        this.id = id;
        startServer(NetUtil.idToPort(id));
        connectToServer(NetUtil.getServerIp(), NetUtil.getMasterPort());
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

    private void processMessageFromServer(int port, String data) {
        System.out.println("[From port " + Integer.toString(port) + "] " + data);
    }

    private void sendToFollowers(String data) {
        broadcast(data);
    }

    /**
     * Called when the worker receives a message from the Master
     * 
     * @param data Data to be sent
     */
    @Override
    protected void onReceiveMessageFromServer(String data) {
        processMessageFromServer(NetUtil.getMasterPort(), data);
    }

    /**
     * Sends a data to either the master or one of the connected workers
     * 
     * @param port Port of the server, identifying the destination server
     * @param data Data to be sent
     */
    private void sendToServer(int port, String data) {
        if (port == NetUtil.getMasterPort()) {
            sendToServer(data);
        } else if (clients.containsKey(port)) {
            clients.get(port).sendToServer(data);
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