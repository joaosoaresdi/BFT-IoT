package bft_iot.proxy;

import bft_iot.json.entities.BFTIoTConfig;
import bft_iot.json.entities.FiwareEntity;
import bft_iot.operation.*;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class OperationSet {
    private LinkedList<ContextOperation> operationSet;
    private LinkedList<FiwareEntity> entitiySet;
    private boolean reachedAgreement;
    private String agreementObj;
    private String objectId;

    public OperationSet(ContextOperation op, FiwareEntity fe) {
        this.operationSet = new LinkedList<>();
        this.entitiySet = new LinkedList<>();
        this.reachedAgreement = false;
        this.objectId = op.getObjectId();
        addOperation(op, fe);
    }

    public int getSize() {
        return operationSet.size();
    }
    public void addOperation(ContextOperation op, FiwareEntity fe) {
        operationSet.addLast(op);
        entitiySet.addLast(fe);
    }

    public boolean isUpdate() {
        return operationSet.getFirst() instanceof ContextUpdate;
    }

    public boolean isCreate() {
        return operationSet.getFirst() instanceof ContextCreate;
    }

    public boolean isQuery() {
        return operationSet.getFirst() instanceof ContextQuery;
    }

    public boolean isSubscription() {
        return operationSet.getFirst() instanceof ContextSubscription;
    }

    public boolean isNotification() {
        return operationSet.getFirst() instanceof ContextNotification;
    }

    public boolean canAdd(ContextOperation operation) {
        if(operationSet.getFirst().getClass().equals(operation.getClass())) { // same operation
            if (this.getObjectId().equals(operation.getObjectId())) { // same target object
                return !containsOp(operation);
            }
        }
        return false;
    }

    private boolean containsOp(ContextOperation op) {
        Iterator<ContextOperation> it = operationSet.iterator();
        while(it.hasNext()) {
            ContextOperation crt = it.next();
            if(crt.getCltId().equals(op.getCltId()))
                return true;
        }
        return false;
    }

    public String getAgreementJSON(String newValue) {
        ContextOperation operation = operationSet.getFirst();
        if(isUpdate()) {
            // get ratio; get value
            // get temperature; get value
            // get occupancy; get value
            // get ???; get value
            JSONObject update = operation.getJsonRequest().getJSONObject("ratio");
            try {
                String oldValue = update.get("value").toString();
                String ret = operation.getJsonRequest().toString().replace(oldValue, newValue);
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(isCreate()) {
            String ret = operation.getJsonRequest().toString();
            return ret;
        } else if(isQuery()) {
            String ret = operation.getJsonRequest().toString();
            return ret;
        } else if(isSubscription()) {
            String ret = operation.getJsonRequest().toString();
            return ret;
        } else if(isNotification()) {
            String ret = operation.getJsonRequest().toString();
            return ret;
        }
        return null;
    }

    public String getOperationValue(ContextOperation operation) {
        if(isUpdate()) {
            // get ratio; get value
            // get temperature; get value
            // get occupancy; get value
            // get ???; get value
            JSONObject update = operation.getJsonRequest().getJSONObject("ratio");
            try {
                String ret = update.get("value").toString();
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(isCreate()) {
            JSONObject update = operation.getJsonRequest().getJSONObject("ratio");
            try {
                String ret = update.get("value").toString();
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(isQuery()) {
            String ret = "" + operation.getJsonRequest();
            return ret;
        } else if(isSubscription()) {
            String ret = "" + operation.getJsonRequest();
            return ret;
        } else if(isNotification()) {
            String ret = "" + operation.getJsonRequest();
            return ret;
        }
        return null;
    }

    private boolean validateSubscription(BFTIoTConfig config) {
        List<ContextOperation> filtered_values = new LinkedList<ContextOperation>();
        Object[] vals = operationSet.toArray();
        for (int i = 0; i < vals.length; i++) {
            int count = 0;
            for (int j = 0; j < vals.length; j++) {
                System.out.println();
                String value_i = getOperationValue((ContextOperation) vals[i]);
                String value_j = getOperationValue((ContextOperation) vals[j]);
                if (Math.abs(value_i.compareTo(value_j)) <= config.temp_threshold) {
                    count++;
                }
            }
            if (count > config.f) {
                filtered_values.add((ContextOperation) vals[i]);
            }
        }

        if (filtered_values.size() >= (config.N - config.f)) {
            this.reachedAgreement = true;
            agreementObj = filtered_values.get(0).getJsonRequest().toString();
            return true;
        }
        return false;
    }

    private boolean validateCreate(BFTIoTConfig config) {
        List<FiwareEntity> filtered_values = new LinkedList<FiwareEntity>();
        Object[] vals = entitiySet.toArray();
        for (int i = 0; i < vals.length; i++) {
            int count = 0;
            for (int j = 0; j < vals.length; j++) {
                if(((FiwareEntity)vals[i]).compareTo((FiwareEntity)vals[j], config)) {
                    count++;
                }
            }
            if (count > config.f) {
                filtered_values.add((FiwareEntity)vals[i]);
            }
        }

        if (filtered_values.size() >= (config.N - config.f)) {
            this.reachedAgreement = true;
            agreementObj = filtered_values.get(0).toString();
            return true;
        }
        return false;
    }

    private boolean validateUpdate(BFTIoTConfig config) {
        System.out.println("\t --> validateUpdate : " + objectId);
        List<FiwareEntity> filtered_values = new LinkedList<FiwareEntity>();
        Object[] vals = entitiySet.toArray();
        for (int i = 0; i < vals.length; i++) {
            int count = 0;
            for (int j = 0; j < vals.length; j++) {
                if(((FiwareEntity)vals[i]).compareTo((FiwareEntity)vals[j], config)) {
                    count++;
                }
            }
            if (count > config.f) {
                filtered_values.add((FiwareEntity)vals[i]);
            }
        }

        if (filtered_values.size() >= (config.N - config.f)) {
            this.reachedAgreement = true;
            agreementObj = filtered_values.get(0).toString();
            return true;
        }

        return false;
    }

    private boolean checkAgreement(BFTIoTConfig config) {
        if(isUpdate()) {
            return validateUpdate(config);
        }
        else if(isCreate()) {
            return validateCreate(config);
        }
        else if(isQuery()) {
            //return validateQuery(config);
        }
        else if(isSubscription()) {
            return validateSubscription(config);
        }
        else if(isNotification()) {
            //return validateNotification(config);
        }

        return false;
    }

    public String getAgreementJSON() {
        if (reachedAgreement) {
            return agreementObj.replace("'","\"");
        }
        else return null;
    }

    public String getObjectId() {
        return objectId;
    }

    public boolean hasReachedAgreement(BFTIoTConfig config) {
        if(reachedAgreement) {
            return true;
        }
        else if (operationSet.size() >= config.N-config.f) {
            return checkAgreement(config);
        }
        return false;
    }

    public String getAgreementValue() {
        return agreementObj;
    }
}
