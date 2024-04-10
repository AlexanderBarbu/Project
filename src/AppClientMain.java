import java.util.Scanner;

import Components.AppClient;
import Utility.Logger;

public class AppClientMain {
    
    private static Logger logger = new Logger("AppClientMain");

    public static void main(String[] args) {
        AppClient appClient = new AppClient();
        logger.write("AppClient is up and running");

        Scanner scanner = new Scanner(System.in);

        try {
            AppClient appClientTest = new AppClient();

            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
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
