import java.util.ArrayList;
import java.util.List;

import Components.Worker;
import Network.DynamicRoutingPolicy;
import Network.IRoutingPolicy;
import Utility.Logger;

public class WorkerMain {

    private static Logger logger = new Logger("WorkerMain");

    public static void main(String[] args) {
        int id = Integer.parseInt(args[0]);
        int workerCount = Integer.parseInt(args[1]);

        Worker worker = new Worker(id);
        logger.write("Worker " + id + " is up and running");

        IRoutingPolicy routingPolicy = new DynamicRoutingPolicy();

        List<Integer> leaders = new ArrayList<Integer>();
        for (int i = 0; i < workerCount; ++i) {
            if (routingPolicy.GetAlternativeRouteIndexes(i, workerCount).contains(id)) {
                leaders.add(i);
            }
        }
        List<Integer> alts = leaders;
        logger.write("Number of following: " + alts.size());

        for (int i = 0; i < alts.size(); ++i) {
            logger.write("Following: " + alts.get(i));
            worker.followWorker(alts.get(i));
        }

        while (true) {}
    }

}
