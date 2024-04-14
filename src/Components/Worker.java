package Components;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import Model.Hotel;
import Model.Room;
import Network.Client;
import Network.Hybrid;
import Network.Message;
import Network.MessageBuilder;
import Network.NetUtil;
import Network.Client.WaitMode;

import Utility.DateRange;
import Utility.HotelFilter;
import Utility.Logger;
import Utility.PriceRange;

public class Worker extends Hybrid {

    public Worker() {
    }

    /**
     *
     */
    private class WorkerClient extends Client {
    }

    private class ReducerClient extends Client {
    }

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

    @Override
    protected void onReceiveMessageFromServer(Message message) {
        logger.write("Received message from master");
        processMessageFromServer(NetUtil.getMasterWorkerPort(), message);
    }

    private void sendToServer(int port, Message message) {
        if (port == NetUtil.getMasterWorkerPort()) {
            sendToServer(message);
        } else if (clients.containsKey(port)) {
            clients.get(port).sendToServer(message);
        }
    }

    public void scheduleDeath(int millliseconds) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(millliseconds);
            } catch (InterruptedException ie) {
            }
            this.disconnectFromServer();
        });
        thread.start();
    }

    //------------------------------------returns only Hotels------------------------------------


    //    static List<Hotel> filterHotels(List<Hotel> hotels, HotelFilter filter) {
//            return hotels.stream()
//                    .filter(hotel -> hotelMatchesCriteria(hotel, filter))
//                    .collect(Collectors.toList());
//    }
    private static boolean hotelMatchesCriteria(Hotel hotel, HotelFilter filter) {
        if (!filter.getAreas().contains(hotel._areaID) || hotel.getRating() != filter.getStars()) {
            return false;
        } else {
            LocalDateTime startDate = filter.getDates().get(0).getFrom();
            LocalDateTime endDate = filter.getDates().get(0).getFrom();
            return hotel.getAvailableRoom(filter.getNumberOfPeople(), startDate, endDate, filter.getPriceRange()) != null;
        }
    }

    //------------------------------------returns Hotels + Rooms------------------------------------

    List<Tuple2<Hotel, List<Room>>> filterHotels(List<Hotel> hotels, HotelFilter filter) {
        return hotels.stream()
                .filter(hotel -> hotelMatchesCriteria(hotel, filter))
                .map(hotel -> new Tuple2<>(hotel, filterRooms(hotel, filter)))
                .collect(Collectors.toList());
    }


    private static List<Room> filterRooms(Hotel hotel, HotelFilter filter) {
        List<LocalDateTime> newDates = convertDates(filter.getDates());
        int lowerPrice = filter.getPriceRange().getFrom();
        int upperPrice = filter.getPriceRange().getTo();

        return hotel.getRooms().values().stream()
                .filter(room -> room.checkAvailability(newDates.get(0), newDates.get(1)))
                .filter(room -> lowerPrice <= room.getPrice() && room.getPrice() <= upperPrice)
                .filter(room -> room.capacity() >= filter.getNumberOfPeople())
                .toList();
    }


    public static ArrayList<LocalDateTime> convertDates(ArrayList<DateRange> availableDates) {
        ArrayList<LocalDateTime> result = new ArrayList<>();
        result.add(availableDates.get(0).getFrom());
        result.add(availableDates.get(0).getTo());
        return result;
    }

    class Tuple2<K, V> {
        private K first;
        private V second;

        public Tuple2(K first, V second) {
            this.first = first;
            this.second = second;
        }

        public K getFirst() {
            return first;
        }

        public V getSecond() {
            return second;
        }

        @Override
        public String toString() {
            return first.toString() + second.toString();
        }
    }

}