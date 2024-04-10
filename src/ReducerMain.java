import Components.Reducer;
import Utility.Logger;

public class ReducerMain {
 
    private static Logger logger = new Logger("ReducerMain");

    public static void main(String[] args) {
        Reducer reducer = new Reducer();
        logger.write("Reducer is up and running");
        while (true) {}
    }

}
