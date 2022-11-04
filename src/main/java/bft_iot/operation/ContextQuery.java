package bft_iot.operation;

import org.json.JSONObject;

public class ContextQuery extends ContextOperation {
    protected ContextQuery(String procId, long timestamp, String objectId, JSONObject payload, String service) {
        super(procId, timestamp, objectId, payload, service);
    }
}
