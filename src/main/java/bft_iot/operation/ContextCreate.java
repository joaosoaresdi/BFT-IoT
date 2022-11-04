package bft_iot.operation;

import org.json.JSONObject;

public class ContextCreate extends ContextOperation {

    public ContextCreate(String procId, long timestamp, String objectId, JSONObject payload, String service) {
        super(procId, timestamp, objectId, payload, service);
    }
}
