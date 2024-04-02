public class NetUtil {
    
    public static int idToPort(int workerId) {
        return 5000 + workerId;
    }

    public static int portToId(int port) {
        return port - 5000;
    }    

    public static int getMasterAppPort() {
        return 4500;
    }

    public static int getMasterWorkerPort() {
        return 4600;
    }

    public static int getReducerPort() {
        return 4700;
    }

    public static String getServerIp() {
        return "127.0.0.1";
    }
}
