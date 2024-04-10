import Components.Worker;
import Utility.Logger;

public class WorkerMain {

    private static Logger logger = new Logger("WorkerMain");

    public static void main(String[] args) {
        int id = Integer.parseInt(args[0]);
        Worker worker = new Worker(id);
        logger.write("Worker " + id + " is up and running");

        while (true) {}
    }

}
