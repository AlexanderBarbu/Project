import java.util.*;

/**
 * Used by a router to determine how to reroute messages
 * 
 * @see Router
 */
public interface IRoutingPolicy {

    /**
     * Returns a list of socket indexes that can be used, alternatively to
     * the given index, in order to reroute a message. 
     * 
     * @param index Index of the socket in the group
     * @param numberOfNodes Number of nodes in the group
     * @return The formenetioned list
     */
    public List<Integer> GetAlternativeRouteIndexes(int index, int numberOfNodes);

}
