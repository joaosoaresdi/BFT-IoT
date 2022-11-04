package bft_iot.operation;

import org.json.JSONObject;

public class ContextOperation {
    protected String cltId;
    protected long timestamp;
    protected String objectId;
    protected JSONObject jsonRequest;
    protected String service;

    protected ContextOperation(String procId, long timestamp, String objectId, JSONObject payload, String service) {
        this.cltId = procId;
        this.timestamp = timestamp;
        this.objectId = objectId;
        this.jsonRequest = payload;
        this.service = service;
    }

    public static ContextOperation parseContextOperation(String req) {
        ContextOperation ret = null;
        JSONObject jsonRequest = new JSONObject(req);

        String procId = jsonRequest.getString("procId");
        long timestamp = jsonRequest.getLong("timestamp");
        String operation = jsonRequest.getString("bft_iot/operation");
        String objectId = jsonRequest.getString("object");
        JSONObject payload = jsonRequest.getJSONObject("value");

        String service = null;
        try {
            service = jsonRequest.getString("service");
        } catch (Exception e) {
            // Service can be null is single tenant mode used.
        }

        if(operation.equals("create")) {
            ret = new ContextCreate(procId, timestamp, objectId ,payload, service);
        } else if (operation.equals("update")) {
            ret = new ContextUpdate(procId, timestamp, objectId ,payload, service);
        } else if (operation.equals("query")) {
            ret = new ContextQuery(procId, timestamp, objectId ,payload, service);
        } else if (operation.equals("subscription")) {
            ret = new ContextSubscription(procId, timestamp, objectId ,payload, service);
        } else if (operation.equals("notification")) {
            ret = new ContextNotification(procId, timestamp, objectId ,payload, service);
        }
        return ret;
    }

    public String getCltId() {
        return cltId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public JSONObject getJsonRequest() {
        return jsonRequest;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getService() {
        return service;
    }
}
