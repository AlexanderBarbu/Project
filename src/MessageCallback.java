import java.net.*;

public interface MessageCallback {
    
    void callback(Socket socket, Message message);

}
