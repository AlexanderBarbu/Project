package Components;
import Network.NetUtil;
import Utility.Logger;
import Network.Hybrid;
import Network.Message;
import Network.MessageBuilder;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Reducer extends Hybrid {

    private class ReductionData {

        private Map<String, Integer> values = null;
        private int workersLeft = 0;

        public ReductionData(int countWorkers) {
            workersLeft = countWorkers;
            values = new ConcurrentHashMap<>();
        }

        /**
         * Decreases the number of workers that haven't sent 
         * their calculated data back to the reducer
         * 
         * @return Whether there are more workers left
         */
        public synchronized boolean decreaseWorkersRemaining() {
            --workersLeft;
            logger.write("WORKERS LEFT: " + workersLeft);
            // It may not make sense to return this, but it's
            // needed so we can avoid a race condition
            return workersLeft > 0;
        }

        public Map<String, Integer> getValues() {
            return values;
        }

        public void increase(String key, int amount) {
            if (!values.containsKey(key)) {
                values.put(key, amount);
            } else {
                values.put(key, values.get(key) + amount);
            }
        }

    }

    /**
     * Maps the request ID of the worker messages to the reduced data.
     * 
     * Here's what the reduction process looks like:
     * 
     * Step 1. The Master sends an INIT_REDUCTION message to the Reducer
     * Step 2. The reducer creates an entry in the map
     * Step 3. Everytime a message other than INIT_REDUCTION arrives, the request ID
     *         is matched to the ReductionData, and the value received
     *         (e.g. the 500 in 'area1:500') is added to the 'area1' entry.
     *         When done, we decrease the number of workers we're waiting for to respond
     * Step 4. We repeat step 3 until there are no more workers left.
     * Step 5. We send the response to the Master
     * 
     */
    private Map<Integer, ReductionData> reductionData = new ConcurrentHashMap<>();

    private Logger logger = new Logger("Reducer");

    public Reducer() {
        logger.write("Starting server...");
        startServer(NetUtil.getReducerPort());
        logger.write("Server started.");
        logger.write("Connecting to master...");
        connectToServer(NetUtil.getServerIp(), NetUtil.getMasterReducerPort());
        logger.write("Connection to master successful.");
    }

    @Override
    protected void onReceiveMessageFromServer(Message message) {
        switch (message.getFunctionId()) {
            case Message.INIT_REDUCTION:
                logger.write("Received INIT_REDUCTION message.");
                String[] params = message.getParams();
                // The request Id that the messages containing the
                // map reduce data will have
                int requestId = message.getRequestId();
                // The number of workers that are expected to send
                // data to the reducer
                int workerCount = Integer.parseInt(params[0]);
                initReduction(requestId, workerCount);
                
                logger.write("Sending READY_FOR_REDUCTION to Master");

                MessageBuilder mb = new MessageBuilder();
                mb.setFunctionID(Message.READY_FOR_REDUCTION);
                mb.setRequestId(message.getRequestId());
                mb.addParam(Integer.toString(requestId));
                sendToServer(mb.get());
                break;
            
            default:
                break;
        }
    }

    protected void onReceiveMessageFromClient(Socket socket, Message message) {
        switch (message.getFunctionId()) {
            case Message.TEST_REDUCTION:
            case Message.GET_OWNED_HOTELS:
            case Message.FILTER_HOTELS:
            case Message.GET_RESERVATIONS_PER_AREA:
                logger.write("Got mapreduce message");
                int reqId = message.getRequestId();
                if (reductionData.containsKey(reqId)) {
                    handleReductionRequest(reqId, message.getParams());
                } else {
                    logger.write("ReqID not found in reduction data");
                }
                break;
        }
    }

    /**
     * Initializes the data necessary to reduce the incoming data
     * 
     * @param requestId Request ID of the messages that the workers will send
     * @param workerCount The number of workers that will send messages
     */
    private void initReduction(int requestId, int workerCount) {
        // If for some reason the request id has been used previously,
        // remove it so we can start with a blank canvas
        if (reductionData.containsKey(requestId)) {
            reductionData.remove(requestId);
        }
        ReductionData rdata = new ReductionData(workerCount);
        reductionData.put(requestId, rdata);
    }

    /**
     * Reduces the data received from a worker.
     * 
     * @param requestId The request ID of the reduction
     * @param params The parameters of the message
     */
    private void handleReductionRequest(int requestId, String[] params) {
        logger.write("Reducing for reqId=" + requestId);
        ReductionData rd = reductionData.get(requestId);
        // Reducing the data
        for (String param : params) {
            String[] parts = new String[2];
            int lastColonIndex = param.lastIndexOf(":");
            logger.write("param=" + param);
            parts[0] = param.substring(0, lastColonIndex);
            parts[1] = param.substring(lastColonIndex + 1, param.length());
            //logger.write(param);
            //logger.write("parts[0]=" + parts[0]);
            //logger.write("parts[1]=" + parts[1]);
            //logger.write("--------");
            rd.increase(parts[0], Integer.parseInt(parts[1]));
        }
        // Responding to the server if there are no more workers left
        if (!rd.decreaseWorkersRemaining()) {
            if (rd.values.containsKey("null") && rd.values.size() > 1) {
                rd.values.remove("null");
            }

            MessageBuilder mb = new MessageBuilder();
            mb.setRequestId(requestId);
            addReducedDataToMessage(rd, mb);
            
            logger.write("Sending reduced data to master");
            sendToServer(mb.get());
        }
    }

    /**
     * Adds a parameter in the message for each entry in the reduction data
     * 
     * @param rd
     * @param mb
     */
    private void addReducedDataToMessage(ReductionData rd, MessageBuilder mb) {
        Map<String, Integer> reducedValues = rd.getValues();
        for (String key : reducedValues.keySet()) {
            Integer value = reducedValues.get(key);
            mb.addParam(key + ":" + value);
        }
    }
}
