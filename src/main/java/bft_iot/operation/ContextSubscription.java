package bft_iot.operation;

import org.json.JSONObject;

public class ContextSubscription extends ContextOperation{

    public ContextSubscription(String procId, long timestamp, String objectId, JSONObject payload, String service) {
        super(procId, timestamp, objectId, payload, service);
    }

}
