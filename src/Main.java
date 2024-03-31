import java.net.*;

class TestServer extends Server {

    @Override
    protected void onReceiveMessage(Socket socket, Message message) {
        System.out.println("Server received: " + message.toString());
    }

}

class TestClient extends Client {

    @Override
    protected void onReceiveMessage(Message message) {
        System.out.println("Client received: " + message.toString());
    }

}

class Test
{
    public static void main(String args[])
    {
        TestServer server = new TestServer();
        server.start(1394);

        TestClient client = new TestClient();
        client.connect(NetUtil.getServerIp(), 1394, Client.WaitMode.WAIT_UNTIL_CONNECTS);

        while (true) {
            client.sendToServer(new Message(0, 1, "Hello from client!"));
            try { Thread.sleep(1000); } catch (InterruptedException ie) { }
            server.broadcast(new Message(2, 3, null));
            try { Thread.sleep(1000); } catch (InterruptedException ie) { }
        }
    }
}