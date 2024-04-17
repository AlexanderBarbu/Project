import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import javax.annotation.processing.Filer;

import Components.AppClient;
import Utility.DateRange;
import Utility.FileReader;
import Utility.Logger;

public class AppClientMain {
    
    private static Logger logger = new Logger("AppClientMain");
    private static AppClient appClient = null;

    public static void main(String[] args) {
        appClient = new AppClient();
        logger.write("AppClient is up and running");

        Scanner scanner = new Scanner(System.in);

        try {
            appClient = new AppClient();

            System.out.print("Enter username: "); // username = admin
            String username = scanner.nextLine(); 
            System.out.print("Enter password: "); // password = password
            String password = scanner.nextLine();
            
            if (appClient.login(username, password)) {
                boolean isRunning = true;
                loadData();
                while (isRunning) {
                    System.out.println("\n1. Add hotel");
                    System.out.println("2. Add room");
                    System.out.println("3. Add dates");
                    System.out.println("4. Reserve room");
                    System.out.println("5. Show reservations");
                    System.out.println("6. Quit\n");

                    int choice = Integer.parseInt(scanner.nextLine());
                    
                    switch (choice) {
                        case 1:
                            AddHotel(scanner);
                            break;
                        case 2:
                            AddRoom(scanner);
                            break;
                        case 3:
                            AddDates(scanner);
                            break;
                        case 4:
                            ReserveRoom(scanner);
                            break;
                        case 5:
                            ShowReservations(scanner);
                            break;
                        case 6:
                            isRunning = false;
                            break;        
                    }
                }
            } else {
                logger.write("Wrong credentials");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                scanner.close();
            } catch (Exception e) {

            }
        }

        System.exit(0);
    }

    private static void loadData() {
        try {
            loadHotels();
            loadRooms();
            loadDates();
            loadReservations();
        } catch (FileNotFoundException fnfe) {
            
        }
    }

    private static void loadHotels() throws FileNotFoundException {
        final String hotelPath = "bin/data/init/hotels.txt";

        for (String filepath : FileReader.read(hotelPath).split("\n|\r\n")) {
            logger.write("Loading " + filepath);
            appClient.saveHotel(FileReader.read(filepath));
        }
    }

    private static void loadRooms() throws FileNotFoundException {
        final String roomPath = "bin/data/init/rooms.txt";

        for (String filepath : FileReader.read(roomPath).split("\n|\r\n")) {
            logger.write("Loading " + filepath);
            appClient.saveRoom(FileReader.read(filepath));
        }
    }

    private static void loadDates() throws FileNotFoundException {
        final String datesPath = "bin/data/init/dates.txt";

        for (String filepath : FileReader.read(datesPath).split("\n|\r\n")) {
            logger.write("Loading " + filepath);
            String[] lines = FileReader.read(filepath).split("\n|\r\n");
            try {
                LocalDateTime from = LocalDateTime.parse(lines[2]);
                LocalDateTime to = LocalDateTime.parse(lines[3]);
                appClient.addDate(lines[0], lines[1], new DateRange(from, to));
            } catch (DateTimeParseException dtpe) {
                System.out.println("Invalid local date time given");
            }
        }
    }

    private static void loadReservations() throws FileNotFoundException {
        final String reservationPath = "bin/data/init/reservations.txt";

        for (String filepath : FileReader.read(reservationPath).split("\n|\r\n")) {
            logger.write("Loading " + filepath);
            String[] lines = FileReader.read(filepath).split("\n|\r\n");
            try {
                LocalDateTime from = LocalDateTime.parse(lines[2]);
                LocalDateTime to = LocalDateTime.parse(lines[3]);
                appClient.reserveRoom(lines[0], lines[1], new DateRange(from, to));
            } catch (DateTimeParseException dtpe) {
                System.out.println("Invalid local date time given");
            }
        }
    }

    private static void AddHotel(Scanner scanner) {
        System.out.print("Hotel JSON File Path: ");
        String hotelJsonPath = scanner.nextLine();
        try {
            appClient.saveHotel(FileReader.read(hotelJsonPath));
            System.out.println("Hotel added");
        } catch (FileNotFoundException fnfe) {
            System.out.println("File not found.");
        }
    }
    
    private static void AddRoom(Scanner scanner) {
        System.out.print("Room JSON File Path: ");
        String roomJsonPath = scanner.nextLine();
        try {
            appClient.saveRoom(FileReader.read(roomJsonPath));
            System.out.println("Room added");
        } catch (FileNotFoundException fnfe) {
            System.out.println("File not found");
        }
    }    
    
    private static void AddDates(Scanner scanner) {
        System.out.print("Hotel name: ");
        String hotelName = scanner.nextLine();

        System.out.print("Room name: ");
        String roomName = scanner.nextLine();

        try {
            System.out.print("Start date: ");
            LocalDateTime from = LocalDateTime.parse(scanner.nextLine());
            System.out.print("End date: ");
            LocalDateTime to = LocalDateTime.parse(scanner.nextLine());

            appClient.addDate(hotelName, roomName, new DateRange(from, to));
        } catch (DateTimeParseException dtpe) {
            System.out.println("Invalid local date time given");
        }
    }
    
    private static void ReserveRoom(Scanner scanner) {
        System.out.print("Hotel name: ");
        String hotel = scanner.nextLine();
        System.out.print("Room name: ");
        String room = scanner.nextLine();

        try {
            System.out.print("Start date: ");
            LocalDateTime from = LocalDateTime.parse(scanner.nextLine());
            System.out.print("End date: ");
            LocalDateTime to = LocalDateTime.parse(scanner.nextLine());

            appClient.reserveRoom(hotel, room, new DateRange(from, to));
        } catch (DateTimeParseException dtpe) {
            System.out.println("Invalid local date time given");
        }
    }
    
    private static void ShowReservations(Scanner scanner) {
        try {
            System.out.print("Start date: ");
            LocalDateTime from = LocalDateTime.parse(scanner.nextLine());
            System.out.print("End date: ");
            LocalDateTime to = LocalDateTime.parse(scanner.nextLine());

            appClient.getReservationCountPerArea(new DateRange(from, to));
        } catch (DateTimeParseException dtpe) {
            System.out.println("Invalid local date time given");
        }
    }
}
