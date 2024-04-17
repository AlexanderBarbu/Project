package Components;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import Authentication.UserManager;
import DAO.HotelDAO;
import DAO.HotelmDAO;
import DAO.UserDAO;
import DAO.UsermDAO;
import Model.Hotel;
import Model.Reservation;
import Model.Room;
import Model.User;
import Network.Client;
import Network.Hybrid;
import Network.Message;
import Network.MessageBuilder;
import Network.NetUtil;
import Network.Client.WaitMode;

import Utility.DateRange;
import Utility.HotelFilter;
import Utility.RoomFilter;
import Utility.Logger;
import Utility.PriceRange;

public class Worker extends Hybrid {

    private class WorkerData {
        HotelDAO hotelDAO = new HotelmDAO();
    }

    public Worker() {

    }

    /**
     *
     */
    private class WorkerClient extends Client {
        @Override
        protected void onReceiveMessage(Message message) {
            super.onReceiveMessage(message);
            logger.write("Received data from leader");
            for (String param : message.getParams()) {
                logger.write(param);
            }
            Worker.this.processMessageFromServer(this.getPort(), message);
        }
    }

    private class ReducerClient extends Client {}

    private int id = -1;
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
    private HashMap<Integer, WorkerData> workerData = new HashMap<>();

    private Logger logger = null;

    public Worker(int id) {
        logger = new Logger("Worker " + id);
        this.id = id;
        workerData.put(id, new WorkerData());
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

    public WorkerData getDataCopy() {
        return this.workerData.get(this.id);
    }

    /**
     * Establishes a connection with the given worker and replicates its data
     *
     * @param id The id of the worker to be followed
     * @see WorkerClient
     */
    public void followWorker(int id) {
        if (id < 0) {
            return;
        }

        logger.write("Following worker with id=" + id);

        final int leaderId = id;

        if (!workerData.containsKey(leaderId)) {
            WorkerClient client = new WorkerClient();
            client.connect(NetUtil.getServerIp(), NetUtil.idToPort(leaderId), Client.WaitMode.WAIT_UNTIL_CONNECTS);
            logger.write("Connected to worker " + leaderId);
            clients.put(leaderId, client);
            workerData.put(leaderId, new WorkerData());
        }
    }

    private void processMessageFromServer(int port, Message message) {
        logger.write("========RECEIVED========");
        logger.write(message.toString());
        switch (message.getFunctionId()) {
            case Message.TEST_REDUCTION: // MapReduce
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

            case Message.GET_OWNED_HOTELS: { // MapReduce
                logger.write("[GET_OWNED_HOTELS] Processing for " + message.getParams()[0]);
                List<Hotel> hotels = getOwnedHotels(message.getParams()[0]);

                MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.setFunctionID(Message.GET_OWNED_HOTELS);
                messageBuilder.setRequestId(message.getRequestId());

                if (hotels.size() > 0) {
                    for (Hotel hotel : hotels) {
                        messageBuilder.addParam(hotel.toString() + ":1");
                        logger.write(hotel.toString() + ":1");
                    }
                } else {
                    messageBuilder.addParam("null:1");
                }
                reducerClient.sendToServer(messageBuilder.get());
                break;
            }
            
            case Message.SAVE_HOTEL:
                logger.write("Saving hotel");
                logger.write(message.toString());
                logger.write("Has " + workerData.keySet().size() + " keys");
                saveHotel(new Hotel(message.getParams()[2]));
                break;

            case Message.COPY_HOTEL: {
                logger.write("Copying hotel sent from worker " + NetUtil.portToId(port));
                Hotel hotel = new Hotel(message.getParams()[1]);
                logger.write(hotel.toString());
                workerData.get(NetUtil.portToId(port)).hotelDAO.save(hotel);
                break;
            }

            case Message.SAVE_ROOM:
                logger.write("Saving room");
                logger.write(message.getParams()[2]);
                saveRoom(new Room(message.getParams()[2]));
                break;

            case Message.COPY_ROOM: {
                logger.write("Copying room sent from worker " + NetUtil.portToId(port));
                logger.write(message.getParams()[0]);
                logger.write(message.getParams()[1]);
                Room room = new Room(message.getParams()[1]);
                workerData.get(NetUtil.portToId(port)).hotelDAO.find(room.getHotelName()).addRoom(room);
                break;
            }

            case Message.RESERVE_ROOM: 
                logger.write("Reserving room");
                logger.write(message.toString());
                saveReservation(message);
                break;
            
            case Message.COPY_RESERVATION: {
                logger.write("Copying room sent from worker " + NetUtil.portToId(port));
                WorkerData wd = workerData.get(NetUtil.portToId(port));
                if (wd == null) {
                    logger.write("Copy failed. WorkerData returned null");
                } else {
                    logger.write("Debugging...");
                    for (String param : message.getParams()) {
                        logger.write(param);
                    }
                }
                break;
            }

            case Message.ADD_DATES: {
                WorkerData wd = workerData.get(id);
                if (wd == null) {
                    logger.write("[ADD_DATES] wd is null");
                } else {
                    String[] params = message.getParams();
                    Hotel hotel = wd.hotelDAO.find(params[2]);
                    if (hotel == null) {
                        logger.write("Hotel not found");
                    } else {
                        Room room = hotel.getRoom(params[3]);
                        if (room == null) {
                            logger.write("Room not found");
                        } else {
                            DateRange dr = new DateRange(params[4]);
                            if (room.addAvailableReservationDates(dr)) {
                                logger.write("Successfully added");
                            } else {
                                logger.write("Failed");
                            }
                        }
                    }
                }
                break;
            }

            case Message.FILTER_HOTELS: {
                int id = Integer.parseInt(message.getParams()[0]);
                WorkerData wd = workerData.get(id);
                if (wd != null) {
                    for (String param : message.getParams()) {
                        System.out.println("[FILTER_HOTELS] " + param);
                    }
                    HotelFilter hotelFilter = new HotelFilter(message.getParams()[1]);
                    List<Hotel> hotels = filterHotels(wd.hotelDAO.findAll(), hotelFilter);
                    MessageBuilder builder = new MessageBuilder();
                    builder.setFunctionID(Message.FILTER_HOTELS);
                    builder.setRequestId(message.getRequestId());
                    for (Hotel hotel : hotels) {
                        builder.addParam(hotel.toString() + ":1");
                    }
                    builder.addParam("null:1");
                    logger.write("Sending to reducer");
                    reducerClient.sendToServer(builder.get());
                } else {
                    logger.write("[Message.FILTER_HOTELS] wd is null");
                }
                break;
            }         
            
            case Message.GET_RESERVATIONS_PER_AREA: {
                logger.write("GET_RESERVATION_PER_AREA");
                WorkerData wd = workerData.get(Integer.parseInt(message.getParams()[0]));
                if (wd != null) {
                    ArrayList<Hotel> hotels = wd.hotelDAO.findAll();
                    HashMap<String, Integer> perAreaMap = new HashMap<>();
                    DateRange dateRange = new DateRange(message.getParams()[1]);
                    logger.write("Have " + hotels.size() + " hotels in memory");
                    for (Hotel hotel : hotels) {
                        logger.write("Hotel " + hotel.Name + " has " + hotel.getRooms().size() + " rooms");
                        for (String roomName : hotel.getRooms().keySet()) {
                            Room room = hotel.getRoom(roomName);
                            logger.write("Room has" + room.getreservations().size() + " reservations");
                            for (Reservation reservation : room.getreservations()) {
                                if (reservation.dateRange.intersects(dateRange)) {
                                    logger.write("Reservation intersects");
                                    String areaId = hotel.areaID();
                                    if (!perAreaMap.containsKey(areaId)) {
                                        perAreaMap.put(areaId, 1);
                                    } else {
                                        perAreaMap.put(areaId, perAreaMap.get(areaId) + 1);
                                    }
                                }
                            }
                        }
                    }
                    perAreaMap.put("null", 1);
                    MessageBuilder builder = new MessageBuilder();
                    builder.setRequestId(message.getRequestId());
                    builder.setFunctionID(message.getFunctionId());
                    for (String area : perAreaMap.keySet()) {
                        builder.addParam(area + ":" + perAreaMap.get(area));
                    }
                    reducerClient.sendToServer(builder.get());
                } else {
                    logger.write("[GET_RESERVATIONS_PER_AREA] wd is null");
                }
                break;
            }
        }
    }

    private List<Hotel> getOwnedHotels(String username) {
        ArrayList<Hotel> hotels = new ArrayList<>();
        User user = UserManager.getUser(username);
        ArrayList<Hotel> storedHotels = workerData.get(this.id).hotelDAO.findAll();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        for (Hotel storedHotel : storedHotels) {
            System.out.println("storedID=" + storedHotel.managerID());
            System.out.println("userID=" + user.Name);
            if (storedHotel.managerID().equals(user.Name)) {
                hotels.add(storedHotel);
            }
        }
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return hotels;
    }

    private void saveHotel(Hotel hotel) {
        logger.write("SAVING HOTEL!!!!");
        WorkerData data = workerData.get(id);
        if (data != null && data.hotelDAO != null) {
            data.hotelDAO.save(hotel);
        }
        logger.write("Forwarding hotel");
        forwardToFollowers(hotel);
    }

    private void saveRoom(Room room) {
        WorkerData data = workerData.get(id);
        if (data != null && data.hotelDAO != null) {
            Hotel hotel = data.hotelDAO.find(room.getHotelName());
            if (hotel != null) {
                hotel.addRoom(room);
                logger.write("Forwarding room");
                forwardToFollowers(room);
            } else {
                logger.write("[saveRoom] Hotel not found");
            }
        }
    }

    private void saveReservation(Message message) {
        String[] params = message.getParams();
        String hotelName = params[2];
        String roomName = params[3];
        logger.write("hotelName=" + hotelName);
        logger.write("roomName=" + roomName);
        logger.write(""+id);
        WorkerData wd = workerData.get(id);
        if (wd==null) logger.write("[saveReservation] wd is null");
        Hotel hotel = wd.hotelDAO.find(hotelName);
        if (hotel != null) {
            Room room = hotel.getRoom(roomName);
            if (room != null) {
                Reservation reservation = new Reservation(
                    UserManager.getUser(params[0]),
                    hotel,
                    room,
                    new DateRange(LocalDateTime.parse(params[4]), LocalDateTime.parse(params[5]))
                );
                logger.write(reservation.toString());
                if (room.isReservationPossible(reservation)) {
                    room.addReservation(reservation);
                    forwardToFollowers(reservation);
                } else {
                    logger.write("Can't reserve room");
                }
            }
        } else {
            logger.write("Hotel not found");
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

    private void forwardToFollowers(Hotel hotel) {
        logger.write(hotel.toString());

        MessageBuilder mb = new MessageBuilder();
        mb.setFunctionID(Message.COPY_HOTEL);
        mb.addParam(hotel.toString());
        broadcast(mb.get());
    }

    private void forwardToFollowers(Room room) {
        logger.write(room.toString());

        MessageBuilder mb = new MessageBuilder();
        mb.setFunctionID(Message.COPY_ROOM);
        mb.addParam(room.toString());
        broadcast(mb.get());
    }

    private void forwardToFollowers(Reservation reservation) {
        logger.write(reservation.toString());

        MessageBuilder mb = new MessageBuilder();
        mb.setFunctionID(Message.COPY_RESERVATION);
        mb.addParam(reservation.toString());
        broadcast(mb.get());
    }

    private static boolean hotelMatchesCriteria(Hotel hotel, HotelFilter filter) {
        if (!filter.getAreas().contains(hotel._areaID) || hotel.getRating() < filter.getStars()) {
            return false;
        } else {
            return true;
            //LocalDateTime startDate = filter.getDates().get(0).getFrom();
            //LocalDateTime endDate = filter.getDates().get(0).getTo();
            //return hotel.getAvailableRoom(filter.getNumberOfPeople(), startDate, endDate, filter.getPriceRange()) != null;
        }
    }

    //------------------------------------returns Hotels + Rooms------------------------------------

    List<Hotel> filterHotels(List<Hotel> hotels, HotelFilter filter) {
        return hotels.stream()
            .filter(hotel -> (hotel.Name == filter.getName()) || filter.getName().isEmpty())
            .filter(hotel -> hotel.NoReviews >= filter.getNumberOfReviews())
            .filter(hotel -> filter.getAreas().contains(hotel.areaID()))
            .filter(hotel -> hotel.getRating() >= filter.getRating())
            .filter(hotel -> hotel.getRooms().size() >= filter.getNumberOfRooms())
            .collect(Collectors.toList());
    }

    List<Tuple2<Hotel, List<Room>>> filterHotels(List<Hotel> hotels, HotelFilter hotelFilter, RoomFilter roomFilter) {
        return hotels.stream()
                .filter(hotel -> hotelMatchesCriteria(hotel, hotelFilter))
                .map(hotel -> new Tuple2<>(hotel, filterRooms(hotel, roomFilter)))
                .collect(Collectors.toList());
    }

    private static List<Room> filterRooms(Hotel hotel, RoomFilter filter) {
        List<LocalDateTime> newDates = convertDates(filter.getDates());
        float lowerPrice = filter.getPriceRange().getFrom();
        float upperPrice = filter.getPriceRange().getTo();

        return hotel.getRooms().values().stream()
                .filter(room -> room.checkAvailability(newDates.get(0), newDates.get(1)))
                .filter(room -> lowerPrice <= room.getPrice() && room.getPrice() <= upperPrice)
                .filter(room -> room.capacity() >= filter.getNumberOfPeople())
                .collect(Collectors.toList());
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