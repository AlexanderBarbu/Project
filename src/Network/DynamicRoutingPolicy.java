package Network;

import java.util.*;
import Utility.Logger;

public class DynamicRoutingPolicy implements IRoutingPolicy{
    
    /**
     * Returns a number of indexes that grows as the number of nodes grows
     */
    public List<Integer> GetAlternativeRouteIndexes(int index, int numberOfNodes) {
        final int numberOfReplicas = numberOfNodes - 1;
        ArrayList<Integer> alternatives = new ArrayList<>();

        for (int i = 1; i <= numberOfReplicas; ++i) {
            alternatives.add((index + i) % numberOfNodes);
        }

        return alternatives;
    }

}
