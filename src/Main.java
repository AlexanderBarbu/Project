import java.util.Scanner;
import java.net.*;

class AppClientTest extends Client {

    private String username = null;

    public AppClientTest(String username, String password) {
        this.username = username;

        System.out.println("Connecting...");
        connect(NetUtil.getServerIp(), NetUtil.getMasterAppPort(), WaitMode.WAIT_UNTIL_CONNECTS);

        System.out.println("Requesting login...");

        MessageBuilder mb = new MessageBuilder();
        mb.setFunctionID(Message.REQUEST_LOGIN);
        mb.addParam(username);
        mb.addParam(StringHasher.Hash(password));

        mb.setCallback((Socket socket, Message response) -> {
            int functionid = response.getFunctionId();
            if (functionid == Message.LOGIN_ACCEPTED) {
                System.out.println("Login success, pinging...");
                String authToken = response.getParams()[0];

                MessageBuilder builder = new MessageBuilder();
                builder.setFunctionID(Message.PING);
                builder.addParam(username);
                builder.addParam(authToken);
                builder.setCallback((s, r) -> {
                    if (r.getFunctionId() == Message.PING) {
                        System.out.println("Server says: " + r.getParams()[0]);
                    }
                });
                sendToServer(builder.get());
            } else if (functionid == Message.LOGIN_REJECTED) {
                System.out.println("Login failed: " + response.getParams()[0]);
            }
        });

        sendToServer(mb.get());
    }

}

class Test
{
    public static void main(String args[])
    {
        Master master = new Master();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        scanner.close();

        AppClientTest appClientTest = new AppClientTest(username, password);

        while (true) {}
    }
}