import Utility.Logger;

import java.util.Scanner;;

class Test
{
    public static Logger logger = new Logger("Test");

    public static void startupServers() {
        logger.write("Creating master...");
        Master master = new Master();
        logger.write("Master created.");
        logger.write("Creating reducer");
        Reducer reducer = new Reducer();
        logger.write("Reducer created.");
        logger.write("Creating workers...");
        for (int i = 0; i < 5; ++i) {
            Worker worker = new Worker(i);
        }
    }

    public static void main(String args[])
    {
        /* 
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
        */
        
        Scanner scanner = new Scanner(System.in);

        try {
            startupServers();

            AppClient appClientTest = new AppClient();

            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            if (appClientTest.login(username, password)) {
                logger.write("Logged in");
                appClientTest.testMapReduce();
            } else {
                logger.write("Wrong credentials");
            }
        } catch (Exception e) {
            
        } finally {
            try {
                scanner.close();
            } catch (Exception e) {

            }
        }


        while (true) {}
    }
}