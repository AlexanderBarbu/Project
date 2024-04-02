import java.security.InvalidParameterException;

public class MessageBuilder {

    private static int global_req_id = 0;

    private StringBuilder sb = new StringBuilder();
    private int functionId = Message.PING;
    private int requestId = -1;

    public void addParam(String param) {
        if (param != null) {
            sb.append(param);
            sb.append('|');
        }
    }

    public Message get() {
        return new Message(
            requestId == -1 ? generateRequestId() : requestId,
            functionId,
            sb.toString()
        );
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

    private synchronized int generateRequestId() {
        return ++global_req_id;
    }
}
