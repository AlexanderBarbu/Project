import Components.Master;
import Utility.Logger;

public class MasterMain {
    
    private static Logger logger = new Logger("MasterMain");

    public static void main(String[] args) {
        Master master = new Master();
        logger.write("Master is up and running");
        while (true) {}
    }

}
