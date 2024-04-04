import java.security.InvalidParameterException;

/**
 * A class used to extract information from a data string
 * and to create a string containing all its data
 * 
 * This class must contain at LEAST the following:
 * 
 * RequestID  - The ID number of the request. A request
 *              can be, for example, a request to all
 *              workers to apply a filter and return
 *              all hotels that match the given criteria.
 *              If another request happens at the same time
 *              by anothere client, we can distinguish the
 *              data of these two requests by using this ID
 * 
 * FunctionID - The ID of the function that the worker must
 *              apply. This is defined by the person implementing
 *              the forementioned function. Codes from 0 to 99 
 *              are reserved for standard functions
 * 
 * Params     - The parameters of the function. The format of
 *              the parameters is defined by the person 
 *              who imlpements the function.
 */
public class Message {   

    public static final int REQUEST_DISCONNECT = 0;
    public static final int REQUEST_LOGIN = 1;
    public static final int REQUEST_REGISTRATION = 2;
    public static final int LOGIN_REJECTED = 3;
    public static final int LOGIN_ACCEPTED = 4;
    public static final int REGISTRATION_ACCEPTED = 5;
    public static final int REGISTRATION_REJECTED = 6;
    public static final int INVALID_PARAMS = 7;
    public static final int INVALID_TOKEN = 8;
    public static final int PING = 9;

    public static final int INVALID_ID = -1;
    
    private int requestId = INVALID_ID;
    private int functionId = INVALID_ID;
    private String params = "";
    private MessageCallback callback = null;

    public Message(String data) {
        extractMembersFromData(data);
    }

    public Message(int reqId, int funcId, String params) {
        this.requestId = reqId;
        this.functionId = funcId;
        if (params == null) {
            params = "";
        }
        this.params = params;
    }

    private void extractMembersFromData(String data) {
        // This pattern is basically: a|b|c
        // Where: a is a non negative integer
        //        b is a non negative integer
        //        c is a string (potentially empty)
        if (data == null || !data.matches("^\\d+\\|\\d+\\|.*$")) {
            throw new InvalidParameterException();
        }
        final int firstPipeIndex = data.indexOf('|');
        final int secondPipeIndex = data.indexOf('|', firstPipeIndex + 1);

        requestId = Integer.parseInt(data.substring(0, firstPipeIndex));
        functionId = Integer.parseInt(data.substring(firstPipeIndex + 1, secondPipeIndex));
        params = data.substring(secondPipeIndex + 1);
    }

    @Override
    public String toString() {
        return String.format("%d|%d|%s", requestId, functionId, params);
    }

    public void setCallback(MessageCallback callback) {
        this.callback = callback;
    }

    public MessageCallback getCallback() {
        return this.callback;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getFunctionId() {
        return functionId;
    }

    public String[] getParams() {
        return params.split("\\|");
    }
}
