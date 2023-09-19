package bft_iot.proxy;

import bft_iot.json.entities.*;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import com.google.gson.Gson;
import bft_iot.operation.ContextCreate;
import bft_iot.operation.ContextOperation;
import bft_iot.operation.ContextUpdate;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class BFT_Broker_Proxy extends DefaultSingleRecoverable {
    private BFTIoTConfig config;

    private int procId;
    // replica state (i.e., context information)
    private HashMap<String, FiwareEntity> contextMap; // (contextId, FiwareEntity) -> context information

    private LinkedList<OperationSet> operationHistory; // (opId, bft_iot.proxy.OperationSet) -> set of replica requests for operation opId

    private int brokerPort;

    private ServiceReplica serviceReplica;

    public BFT_Broker_Proxy(int procId, int brokerPort) {
        this.procId = procId;
        this.brokerPort = brokerPort;
        this.operationHistory = new LinkedList<OperationSet>();
        //this.pending_transactions = new HashMap<Long, ReceivedData>();
        this.contextMap = new HashMap<String, FiwareEntity>();
        this.serviceReplica = new ServiceReplica(procId, this, this);
        config = BFTIoTConfig.loadConfiguration(procId);
    }

    private OperationSet addToPending (ContextOperation operation, FiwareEntity fe) {
        Iterator<OperationSet> it = operationHistory.iterator();
        while(it.hasNext()) {
            OperationSet crt = it.next();
            System.out.println("################## addToPending: " + fe);
            System.out.println(crt);
            if (crt.isInstanceOf(fe)) {
                if (crt.canAdd(operation)) {
                    crt.addOperation(operation, fe);
                    return crt;
                }
            }
        }
        OperationSet set = new OperationSet(operation, fe);
        operationHistory.addLast(set);
        return set;
        /*
        if(operationHistory.containsKey(operation.getOpId())) {
            set = operationHistory.get(operation.getOpId());
        } else {
            set = new bft_iot.proxy.OperationSet(operation.getOpId(), operation.getObjectId());
            operationHistory.put(operation.getOpId(), set);
        }

        set.addOperation(operation, fe);
        return set;
         */
    }

    /*
    private ReceivedData addObject2Map(DataMessage data) {
        if(data.value.contains("type")) {
            String val = data.value.replace("|", "\"");
            val = val.substring(1, val.length()-1);

            JSONObject ratioObject = new JSONObject(val).getJSONObject("ratio");
            String value = ratioObject.getString("value");
            data.setValue(value);
        }

        ReceivedData subset;
        synchronized (pending_transactions) {
            if(pending_transactions.containsKey(data.opID)) {
                subset = pending_transactions.get(data.opID);
            }
            else {
                subset = new ReceivedData(data.opID);
                pending_transactions.put(data.opID, subset);
            }
        }
        subset.getAnswerSet().put(data.procID, data);
        return subset;
    }

    public void ValidateAndSend(bft_iot.proxy.OperationSet opSet) {
        if(!opSet.hasReachedAgreement(config)) {
            List<DataMessage> filtered_values = new LinkedList<DataMessage>();
            Object[] vals = opSet.getSet().toArray();
            for (int i = 0; i < vals.length; i++) {
                int add = 0;
                for (int j = 0; j < vals.length; j++) {
                    if (Math.abs(Double.parseDouble(((DataMessage)vals[i]).value) - Double.parseDouble(((DataMessage)vals[j]).value)) < config.threshold) {
                        add++;
                    }
                }
                if (add > config.f) {
                    filtered_values.add((DataMessage)vals[i]);
                }
            }

            if (filtered_values.size() >= (config.N - config.f)) {
                opSet.setReachedAgreement(true);
                double sum = 0.0;
                for (DataMessage dd : filtered_values) {
                    sum += Double.parseDouble(dd.value);
                }
                //Todo: Forward data to Gateway or Broker
                System.out.println("Fowarding value : " + (sum/filtered_values.size()) + " after (" + vals.length + " received values)");
            }
        }
        if (opSet.size() == config.N) {
            operationHistory.remove(opSet.getOpId());
        }
    }
     */

    @Override
    public void installSnapshot(byte[] bytes) {

    }

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    Gson gson = new Gson();

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext messageContext) {
        String cmd = new String(command);
        System.out.println("appExecuteOrdered : " +  cmd);

        ContextOperation op = ContextOperation.parseContextOperation(cmd);
        FiwareEntity fe = null;
        try {
            if (op instanceof ContextCreate) {
                if(op.getJsonRequest().getString("id").contains("humidity")) {
                    fe = gson.fromJson(op.getJsonRequest().toString(), HumiditySensorEntity.class);
                }
                else if (op.getJsonRequest().getString("id").contains("trash")) {
                    fe = gson.fromJson(op.getJsonRequest().toString(), TrashSensorEntity.class);
                }
                else if (op.getJsonRequest().getString("id").contains("parking")) {
                    fe = gson.fromJson(op.getJsonRequest().toString(), ParkingSensorEntity.class);
                }
            }
            else if (op instanceof ContextUpdate) {
                if (op.getObjectId().contains("humidity")) {
                    fe = gson.fromJson(op.getJsonRequest().toString(), HumidityEntityUpdate.class);
                } else if (op.getObjectId().contains("trash")) {
                    fe = gson.fromJson(op.getJsonRequest().toString(), TrashEntityUpdate.class);
                }
                else if (op.getObjectId().contains("parking")) {
                    fe = gson.fromJson(op.getJsonRequest().toString(), ParkingEntityUpdate.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        OperationSet opSet = addToPending(op, fe);

        if (opSet.hasReachedAgreement(config)) {
            String agreementJSON = opSet.getAgreementJSON();
            if(opSet.isCreate()) {
                System.out.println(" ---> Agreement Value ----> " + agreementJSON);
                HttpResponse ret = execute(config.brokerHostname, config.brokerPort, "v2/entities", "POST", agreementJSON.getBytes(StandardCharsets.UTF_8), op.getService());
                return ret.contents.getBytes(StandardCharsets.UTF_8);
            }
            else if(opSet.isUpdate()) {
                String objectId = opSet.getObjectId();
                System.out.println(objectId + " ---> Agreement Value ----> " + agreementJSON);
                HttpResponse ret = execute(config.brokerHostname, config.brokerPort, String.format("v2/entities/%s/attrs", objectId), "PATCH", agreementJSON.getBytes(StandardCharsets.UTF_8), op.getService());
                return ret.contents.getBytes(StandardCharsets.UTF_8);
            }
            else if (opSet.isQuery()) {
                String objectId = opSet.getObjectId();
                System.out.println(objectId + " ---> Agreement Value ----> " + agreementJSON);
                HttpResponse ret = execute(config.brokerHostname, config.brokerPort, String.format("v2/entities/%s", objectId), "GET", null, op.getService());
                return ret.contents.getBytes(StandardCharsets.UTF_8);
            }
            else if (opSet.isSubscription()) {
                String objectId = opSet.getObjectId();
                System.out.println(objectId + " ---> Agreement Value ----> " + agreementJSON);
                HttpResponse ret = execute(config.brokerHostname, config.brokerPort, "v2/subscriptions", "POST", agreementJSON.getBytes(StandardCharsets.UTF_8), op.getService());
                return ret.contents.getBytes(StandardCharsets.UTF_8);
            }

            if (opSet.getSize() == config.N) {
                operationHistory.remove(opSet);
            }

        }
        return command;
    }

    @Override
    public byte[] appExecuteUnordered(byte[] bytes, MessageContext messageContext) {
        return new byte[0];
    }

    static String create_subscription(String client_app_name, String sensor_type, String[] attrs, String host, int port, String path) {
        String attr_set = "";
        for (String s : attrs) {
            attr_set += "'"+ s+ "',";
        }
        attr_set = attr_set.substring(0, attr_set.length()-1);
        String attrs_string = (attr_set.substring(1,attr_set.length()-1)).replace("\'","");

        String description = String.format("{'description':'Subscription from %s to %s sensors on %s','subject':{'entities':[{'idPattern':'.*','type':'%s'}],'condition':{'attrs':[%s]}},'notification':{'http':{'url':'%s'},'attrs':[%s]},'expires':'2040-01-01T14:00:00.00Z'}", client_app_name, sensor_type, attrs_string, sensor_type, attr_set, String.format("http://%s:%d/%s", host, port, path), attr_set);
        return description.replace('\'', '\"');
    }

    static class HttpResponse {
        String contents;
        int respCode;

        HttpResponse(String contents, int code) {
            this.contents = contents;
            this.respCode = code;
        }
    }

    static HttpResponse execute(String host, int port, String path, String http_method, byte[] data, String service) {
        try {
            URL url = new URL(String.format("http://%s:%d/%s", host, port, path));
            System.out.println(url.toString());
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            if (http_method.equals("GET")) {
                http.setRequestMethod(http_method);
                http.addRequestProperty("Accept","application/json");
            } else if (http_method.equals("PATCH")) {
                http.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                http.setRequestMethod("POST");
            } else {
                http.setRequestMethod(http_method);
            }
            if (service != null) {
                http.addRequestProperty("Fiware-Service", service);
            }
            if (data != null) {
                http.addRequestProperty("Content-Type","application/json");
                http.addRequestProperty("Content-Length", ""+ data.length);
                http.setDoOutput(true);
            }
            http.connect();
            if (data != null) {
                OutputStream os = http.getOutputStream();
                os.write(data);
                os.flush();
                os.close();
            }
            println(String.valueOf(http.getResponseCode()));
            println(http.getResponseMessage());
            if (http_method.equals("GET")) {
                return new HttpResponse(http.getContent().toString(), http.getResponseCode());
            }
            return new HttpResponse(http.getResponseMessage(), http.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HttpResponse("[execute] connection error", 500);
    }

    static void print(String s) {
        println(s);
    }

    static void println(String s) {
        System.out.println(s);
    }

    static void subscribe_Broker(String host, int port, byte[] data, String service) {
        execute(host, port, "v2/subscriptions/", "POST", data, service);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ... bft_iot.proxy.BFT_Broker_Proxy <process id> <broker port>");
            System.out.println("       process id: process identifier corresponds directly with ServiceProxy id");
            System.out.println("       broker port: Context Broker port number");
            System.exit(-1);
        }

        int id = Integer.parseInt(args[0]);
//        int broker_port = Integer.parseInt(args[1]);
        int broker_port = 1020;
        BFT_Broker_Proxy proxy = new BFT_Broker_Proxy(id, broker_port);
    }
}

