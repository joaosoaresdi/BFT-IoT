package bft_iot.proxy;

import bft_iot.json.entities.*;
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

    public boolean isInstanceOf(FiwareEntity fe) {
        if (entitiySet.isEmpty() || fe == null)
                return true;
        if (entitiySet.getFirst() == null)
            return true;
        return entitiySet.getFirst().getClass().equals(fe.getClass());
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
        if (operationSet.getFirst().getClass().equals(operation.getClass())) { // same operation
            if (this.getObjectId().equals(operation.getObjectId())) { // same target object
                return !containsOp(operation);
            }
        }
        return false;
    }

    private boolean containsOp(ContextOperation op) {
        Iterator<ContextOperation> it = operationSet.iterator();
        while (it.hasNext()) {
            ContextOperation crt = it.next();
            if (crt.getCltId().equals(op.getCltId()))
                return true;
        }
        return false;
    }

    public String getAgreementJSON(String newValue) {
        ContextOperation operation = operationSet.getFirst();
        if (isUpdate()) {
            // get ratio; get value
            // get temperature; get value
            // get occupancy; get value
            // get ???; get value\
            JSONObject update = null;
            if (this.entitiySet.getFirst() instanceof HumiditySensorEntity) {
                update = operation.getJsonRequest().getJSONObject("temperature");
            } else if (this.entitiySet.getFirst() instanceof ParkingSensorEntity) {
                update = operation.getJsonRequest().getJSONObject("occupancy");
            } else if (this.entitiySet.getFirst() instanceof TrashSensorEntity) {
                update = operation.getJsonRequest().getJSONObject("ratio");
            }
            try {
                String oldValue = update.get("value").toString();
                String ret = operation.getJsonRequest().toString().replace(oldValue, newValue);
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (isCreate()) {
            String ret = operation.getJsonRequest().toString();
            return ret;
        } else if (isQuery()) {
            String ret = operation.getJsonRequest().toString();
            return ret;
        } else if (isSubscription()) {
            String ret = operation.getJsonRequest().toString();
            return ret;
        } else if (isNotification()) {
            String ret = operation.getJsonRequest().toString();
            return ret;
        }
        return null;
    }

    public String getOperationValue(ContextOperation operation) {
        JSONObject update = null;
        if (this.entitiySet.getFirst() instanceof HumiditySensorEntity) {
            update = operation.getJsonRequest().getJSONObject("temperature");
        } else if (this.entitiySet.getFirst() instanceof ParkingSensorEntity) {
            update = operation.getJsonRequest().getJSONObject("occupancy");
        } else if (this.entitiySet.getFirst() instanceof TrashSensorEntity) {
            update = operation.getJsonRequest().getJSONObject("ratio");
        }
        if (isUpdate()) {
            // get ratio; get value
            // get temperature; get value
            // get occupancy; get value
            // get ???; get value
            //JSONObject update = operation.getJsonRequest().getJSONObject("ratio");
            try {
                String ret = update.get("value").toString();
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isCreate()) {
            //JSONObject update = operation.getJsonRequest().getJSONObject("ratio");
            try {
                String ret = update.get("value").toString();
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isQuery()) {
            String ret = "" + operation.getJsonRequest();
            return ret;
        } else if (isSubscription()) {
            String ret = "" + operation.getJsonRequest();
            return ret;
        } else if (isNotification()) {
            String ret = "" + operation.getJsonRequest();
            return ret;
        }
        return null;
    }

    private boolean validateHumiditySub(BFTIoTConfig config) {
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

    private boolean validateParkingSub(BFTIoTConfig config) {
        List<ContextOperation> filtered_values = new LinkedList<ContextOperation>();
        Object[] vals = operationSet.toArray();
        for (int i = 0; i < vals.length; i++) {
            int count = 0;
            for (int j = 0; j < vals.length; j++) {
                System.out.println();
                String value_i = getOperationValue((ContextOperation) vals[i]);
                String value_j = getOperationValue((ContextOperation) vals[j]);
                System.out.println("------- validateParkingSub: " + value_i + ", " + value_j);
                if ( Integer.parseInt(value_i) == Integer.parseInt(value_j) ) {
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

    private boolean validateTrashSub(BFTIoTConfig config) {
        List<ContextOperation> filtered_values = new LinkedList<ContextOperation>();
        Object[] vals = operationSet.toArray();
        for (int i = 0; i < vals.length; i++) {
            int count = 0;
            for (int j = 0; j < vals.length; j++) {
                System.out.println();
                String value_i = getOperationValue((ContextOperation) vals[i]);
                String value_j = getOperationValue((ContextOperation) vals[j]);
                System.out.println("------- validateTrashSub: " + value_i + ", " + value_j);
                if (Math.abs(value_i.compareTo(value_j)) <= config.ratio_threshold) {
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

    private boolean validateSubscription(BFTIoTConfig config) {
        if(entitiySet.getFirst() instanceof HumiditySensorEntity || entitiySet.getFirst() instanceof HumidityEntityUpdate)
            return validateHumiditySub(config);
        else if (entitiySet.getFirst() instanceof ParkingSensorEntity || entitiySet.getFirst() instanceof ParkingEntityUpdate)
            return validateParkingSub(config);
        else
            return validateTrashSub(config);
    }

    private boolean validateCreate(BFTIoTConfig config) {
        List<FiwareEntity> filtered_values = new LinkedList<FiwareEntity>();
        Object[] vals = entitiySet.toArray();
        for (int i = 0; i < vals.length; i++) {
            int count = 0;
            for (int j = 0; j < vals.length; j++) {
                if (((FiwareEntity) vals[i]).compareTo((FiwareEntity) vals[j], config)) {
                    count++;
                }
            }
            if (count > config.f) {
                filtered_values.add((FiwareEntity) vals[i]);
            }
        }

        if (filtered_values.size() >= (config.N - config.f)) {
            this.reachedAgreement = true;
            agreementObj = filtered_values.get(0).toString();
            return true;
        }
        return false;
    }

    private String getHumidAgreementValue(List<FiwareEntity> filtered_values) {
        System.out.println("--------------- getHumidAgreementValue ------------------");
        System.out.println("--------------- getHumidAgreementValue ------------------");
        System.out.println("--------------- getHumidAgreementValue ------------------");
        float humid_sum = 0;
        float temp_sum = 0;

        for (FiwareEntity ent : filtered_values) {
            humid_sum += ((HumidityEntityUpdate) ent).getHumidity().value;
            temp_sum += ((HumidityEntityUpdate) ent).getTemperature().value;
        }
        float humid_avg = humid_sum / filtered_values.size();
        float temp_avg = temp_sum / filtered_values.size();

        HumidityEntityUpdate update = (HumidityEntityUpdate) filtered_values.get(0);
        update.humidity.value = humid_avg;
        update.temperature.value = temp_avg;
        System.out.println("--------------- " + humid_avg);
        System.out.println("--------------- " + temp_avg);

        return update.toString();
    }

    private String getParkAgreementValue(List<FiwareEntity> filtered_values) {
        System.out.println("--------------- getParkAgreementValue ------------------");
        System.out.println("--------------- getParkAgreementValue ------------------");
        System.out.println("--------------- getParkAgreementValue ------------------");
        int occupancy_sum = 0;

        for (FiwareEntity ent : filtered_values) {
            occupancy_sum += ((ParkingEntityUpdate) ent).getOccupancy().value;
        }
        float occupancy_avg = occupancy_sum / filtered_values.size();

        ParkingEntityUpdate update = (ParkingEntityUpdate) filtered_values.get(0);
        update.occupancy.value = Math.round(occupancy_avg);
        System.out.println("--------------- " + occupancy_avg);

        return update.toString();
   }

    private String getAgreementValue(List<FiwareEntity> filtered_values) {
        if (filtered_values.get(0) instanceof HumiditySensorEntity || filtered_values.get(0) instanceof HumidityEntityUpdate)
            return getHumidAgreementValue(filtered_values);
        else if (filtered_values.get(0) instanceof ParkingSensorEntity || filtered_values.get(0) instanceof ParkingEntityUpdate)
            return getParkAgreementValue(filtered_values);
        else
            return "";
    }

    private boolean validateUpdate(BFTIoTConfig config) {
        System.out.println("\t --> validateUpdate : " + objectId);
        List<FiwareEntity> filtered_values = new LinkedList<FiwareEntity>();
        Object[] vals = entitiySet.toArray();
        for (int i = 0; i < vals.length; i++) {
            int count = 0;
            for (int j = 0; j < vals.length; j++) {
                if (((FiwareEntity) vals[i]).compareTo((FiwareEntity) vals[j], config)) {
                    count++;
                }
            }
            if (count > config.f) {
                filtered_values.add((FiwareEntity) vals[i]);
            }
        }

        if (filtered_values.size() >= (config.N - config.f)) {
            this.reachedAgreement = true;
            agreementObj = getAgreementValue(filtered_values);
            //agreementObj = filtered_values.get(0).toString();
            return true;
        }

        return false;
    }

    private boolean checkAgreement(BFTIoTConfig config) {
        if (isUpdate()) {
            return validateUpdate(config);
        } else if (isCreate()) {
            return validateCreate(config);
        } else if (isQuery()) {
            //return validateQuery(config);
        } else if (isSubscription()) {
            return validateSubscription(config);
        } else if (isNotification()) {
            //return validateNotification(config);
        }

        return false;
    }

    public boolean canDelete = false;
    public String getAgreementJSON() {
        if (reachedAgreement) {
            canDelete = true;
            return agreementObj.replace("'", "\"");
        } else return null;
    }

    public String getObjectId() {
        return objectId;
    }

    public boolean hasReachedAgreement(BFTIoTConfig config) {
        if (reachedAgreement) {
            return true;
        } else if (operationSet.size() >= config.N - config.f) {
            return checkAgreement(config);
        }
        return false;
    }

    public String getAgreementValue() {
        return agreementObj;
    }
}
