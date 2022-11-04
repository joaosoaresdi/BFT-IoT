package bft_iot.operation;

import org.json.JSONObject;

public class ContextUpdate extends ContextOperation {

    public ContextUpdate(String procId, long timestamp, String objectId, JSONObject payload, String service) {
        super(procId, timestamp, objectId, payload, service);
    }

}
