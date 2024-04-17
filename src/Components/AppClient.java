package Components;

import Network.Client;
import Network.NetUtil;
import Network.MessageBuilder;
import Network.Message;

import Model.Hotel;
import Model.HotelJSON;
import Model.Room;
import Model.RoomJSON;
import Utility.DateRange;
import Utility.HotelFilter;
import Utility.RoomFilter;
import Utility.Logger;

import com.google.gson.*;

import java.net.*;

public class AppClient extends Client {

    private String username = null;
    private String authToken = null;

    private Logger logger = new Logger("AppClient");

    public AppClient() {
        logger.write("Connecting to Master...");
        connect(NetUtil.getServerIp(), NetUtil.getMasterAppPort(), WaitMode.WAIT_UNTIL_CONNECTS);
        logger.write("Connected to Master");
    }

    @Override
    protected void onReceiveMessage(Message message) {
        logger.write("FuncID=" + message.getFunctionId() + ",ReqID=" + message.getRequestId() + ",Params=" + String.join(";", message.getParams()));
        super.onReceiveMessage(message);
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

    public void saveHotel(String hotelJsonString) {
        logger.write("Saving hotel");

        Gson gson = new Gson();
        HotelJSON hotelJson = gson.fromJson(hotelJsonString, HotelJSON.class);
        Hotel hotel = new Hotel(hotelJson);

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setFunctionID(Message.SAVE_HOTEL);
        messageBuilder.addParam(username);
        messageBuilder.addParam(authToken);
        messageBuilder.addParam(hotel.toString());
        sendToServer(messageBuilder.get());
    }

    public void saveRoom(String roomJsonString) {
        logger.write("Saving room");

        Gson gson = new Gson();
        RoomJSON roomJson = gson.fromJson(roomJsonString, RoomJSON.class);
        Room room = new Room(roomJson);

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setFunctionID(Message.SAVE_ROOM);
        messageBuilder.addParam(username);
        messageBuilder.addParam(authToken);
        messageBuilder.addParam(room.toString());
        sendToServer(messageBuilder.get());
    }

    public void reserveRoom(String hotel, String room, DateRange reservationRange) {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setFunctionID(Message.RESERVE_ROOM);
        messageBuilder.addParam(username);
        messageBuilder.addParam(authToken);
        messageBuilder.addParam(hotel);
        messageBuilder.addParam(room);
        messageBuilder.addParam(reservationRange.getFrom().toString());
        messageBuilder.addParam(reservationRange.getTo().toString());
        sendToServer(messageBuilder.get());
    }

    public void getOwnedHotels() {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setFunctionID(Message.GET_OWNED_HOTELS);
        messageBuilder.setCallback((Socket s, Message r) -> {
            System.out.println("Owned hotels: ");
            for (String param : r.getParams()) {
                System.out.println(param);
            }
        });
        messageBuilder.addParam(username);
        messageBuilder.addParam(authToken);
        sendToServer(messageBuilder.get());
    }

    public void filterHotels(HotelFilter filter) {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setFunctionID(Message.FILTER_HOTELS);
        messageBuilder.setCallback((s, r) -> {
            System.out.println(r.toString());
        });
        messageBuilder.addParam(username);
        messageBuilder.addParam(authToken);
        messageBuilder.addParam(filter.toString());
        sendToServer(messageBuilder.get());
    }

    public void getReservationCountPerArea(DateRange dateRange) {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setFunctionID(Message.GET_RESERVATIONS_PER_AREA);
        messageBuilder.setCallback((s, r) -> {
            String[] params = r.getParams();
            if (params.length == 1 && params[0].split(":")[0].equals("null")) {
                System.out.println("No reservations found");
            } else {
                for (String param : params) {
                    System.out.println(param);
                }
            }
        });
        messageBuilder.addParam(username);
        messageBuilder.addParam(authToken);
        messageBuilder.addParam(dateRange.toString());
        sendToServer(messageBuilder.get());
    }

    public void addDate(String hotelName, String roomName, DateRange range) {
        MessageBuilder mb = new MessageBuilder();
        mb.setFunctionID(Message.ADD_DATES);
        mb.addParam(username);
        mb.addParam(authToken);
        mb.addParam(hotelName);
        mb.addParam(roomName);
        mb.addParam(range.toString());
        sendToServer(mb.get());
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
