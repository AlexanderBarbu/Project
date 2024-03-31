import java.net.*;

class Master extends Server {

}

class Test
{
    public static void main(String args[])
    {
        Master master = new Master();
        master.start(NetUtil.getMasterPort());

        Worker worker1 = new Worker(0);
        Worker worker2 = new Worker(1);                

        worker2.followWorker(worker1);
    
        worker1.scheduleDeath(3000);
        worker2.scheduleDeath(7000);

        while (true) {
            System.out.println("Sending message");
            master.send(0, "hello");

            try { Thread.sleep(2000); } catch (InterruptedException ie) {}
        }
    }
}