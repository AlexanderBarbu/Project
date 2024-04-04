import java.util.Scanner;

import Model.Hotel;
import Network.Client;
import Network.Message;
import Network.MessageBuilder;
import Network.NetUtil;
import Utility.StringHasher;
import Utility.DateRange;
import java.util.*;

import java.net.*;
import java.time.LocalDateTime;

class AppClientTest extends Client {

    private String username = null;

    public AppClientTest(String username, String password) {
        this.username = username;

        System.out.println("Connecting...");
        connect(NetUtil.getServerIp(), NetUtil.getMasterAppPort(), WaitMode.WAIT_UNTIL_CONNECTS);

        System.out.println("Requesting login...");

        MessageBuilder mb = new MessageBuilder();
        mb.setFunctionID(Message.REQUEST_LOGIN);
        mb.addParam(username);
        mb.addParam(StringHasher.Hash(password));

        mb.setCallback((Socket socket, Message response) -> {
            int functionid = response.getFunctionId();
            if (functionid == Message.LOGIN_ACCEPTED) {
                System.out.println("Login success, pinging...");
                String authToken = response.getParams()[0];

                MessageBuilder builder = new MessageBuilder();
                builder.setFunctionID(Message.PING);
                builder.addParam(username);
                builder.addParam(authToken);
                builder.setCallback((s, r) -> {
                    if (r.getFunctionId() == Message.PING) {
                        System.out.println("Server says: " + r.getParams()[0]);
                    }
                });
                sendToServer(builder.get());
            } else if (functionid == Message.LOGIN_REJECTED) {
                System.out.println("Login failed: " + response.getParams()[0]);
            }
        });

        sendToServer(mb.get());
    }

}

class Test
{
    public static void main(String args[])
    {
        Hotel hotel = new Hotel("MyHotel", 5, 100, "1234", "3214");
        System.out.println(hotel.toString());
        Hotel hotelReplica = new Hotel(hotel.toString());
        System.out.println(hotelReplica.Name);
        System.out.println(hotelReplica.areaID());
        System.out.println(hotelReplica.NoReviews);
        System.out.println(hotelReplica.Rating);
        System.out.println(hotelReplica.IsFull);
        System.out.println();

        HotelFilter filter = new HotelFilter();
        filter.addArea("area1");
        filter.addArea("area2");
        filter.addDateRange(LocalDateTime.now(), LocalDateTime.now().plusDays(10));
        filter.addDateRange(LocalDateTime.now().plusDays(20), LocalDateTime.now().plusDays(30));
        filter.setNumberOfPeople(5);
        filter.setPriceRange(100, 1000);
        filter.setStars(3);
        System.out.println(filter.toString());

        HotelFilter filterReplica = new HotelFilter(filter.toString());
        for (String area : filterReplica.getAreas()) {
            System.out.print(area + " ");
        }
        for (DateRange dates : filterReplica.getDates()) {
            System.out.println(dates.getFrom() + " to " + dates.getTo() + " ");
        }
        System.out.println(filter.getNumberOfPeople());
        System.out.println(filter.getPriceRange().getFrom() + " to " + filter.getPriceRange().getTo());
        System.out.println(filter.getStars());

        Master master = new Master();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        scanner.close();

        AppClientTest appClientTest = new AppClientTest(username, password);

        while (true) {}
    }
}