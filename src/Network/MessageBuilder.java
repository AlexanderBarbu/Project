package Network;
import java.security.InvalidParameterException;
import java.util.concurrent.ThreadLocalRandom;

public class MessageBuilder {

    private static int global_req_id = ThreadLocalRandom.current().nextInt(0, 1073741824);

    private StringBuilder sb = new StringBuilder();
    private int functionId = Message.PING;
    private int requestId = -1;
    private MessageCallback callback = null;

    public void setCallback(MessageCallback callback) {
        this.callback = callback;
    }

    public void addParam(String param) {
        if (param != null) {
            sb.append(param);
            sb.append('|');
        }
    }

    public Message get() {
        Message message = new Message(
            requestId == -1 ? generateRequestId() : requestId,
            functionId,
            sb.toString()
        );
        if (callback != null) {
            message.setCallback(callback);
        }
        return message;
    }

    public void setRequestId(int reqId) {
        if (reqId < 0) {
            throw new InvalidParameterException();
        }
        requestId = reqId;
    }

    public void setFunctionID(int funcId) {
        if (funcId < 0) {
            throw new InvalidParameterException();
        }
        functionId = funcId;
    }

    public synchronized int generateRequestId() {
        return ++global_req_id;
    }
}
