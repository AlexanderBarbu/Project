import Network.NetUtil;
import Network.Server;

public class Reducer extends Server {
    
    public Reducer() {
        start(NetUtil.getReducerPort());
    }

}
