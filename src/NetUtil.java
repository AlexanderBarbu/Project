public class NetUtil {
    
    public static int idToPort(int workerId) {
        return 5000 + workerId;
    }

    public static int portToId(int port) {
        return port - 5000;
    }

    public static int getMasterPort() {
        return 4999;
    }

    public static int getReducerPort() {
        return 4998;
    }

    public static String getServerIp() {
        return "127.0.0.1";
    }
}
