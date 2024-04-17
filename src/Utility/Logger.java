package Utility;

public class Logger {
   
    private String id = null;

    public Logger(String id) {
        if (id != null && id.length() > 0) {
            this.id = id;
        } else {
            this.id = Thread.currentThread().getName();
        }
    }

    public synchronized void write(String message) {
        //System.out.println("[" + id + "] " + message);
    }
}
