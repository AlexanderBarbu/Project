import java.util.Scanner;

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
        sendToServer(mb.get());
    }

    @Override
    protected void onReceiveMessage(Message message) {
        switch (message.getFunctionId()) {
            case Message.LOGIN_ACCEPTED:
                System.out.println("Login success, pinging...");
                String authToken = message.getParams()[0];

                MessageBuilder mb = new MessageBuilder();
                mb.setFunctionID(Message.PING);
                mb.addParam(username);
                mb.addParam(authToken);
                sendToServer(mb.get());
                break;

            case Message.LOGIN_REJECTED:
                System.out.println("Login failed: " + message.getParams()[0]);
                break;

            case Message.PING:
                System.out.println("Server says: " + message.getParams()[0]);
                break;
        }
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