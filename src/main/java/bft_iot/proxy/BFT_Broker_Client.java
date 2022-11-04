package bft_iot.proxy;

import bftsmart.tom.ServiceProxy;
import com.google.gson.Gson;
import bft_iot.json.entities.*;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;

import static spark.Spark.*;

public class BFT_Broker_Client {
    private int procId;
    private ServiceProxy proxy;

    private HashMap<String, FiwareEntity> contextMap;
    private HashMap<String, FiwareSubscription> subscriptionMap;

    public BFT_Broker_Client(int procId) {
        this.procId = procId;
        this.proxy = new ServiceProxy(procId);
        this.contextMap = new HashMap<>();
        this.subscriptionMap = new HashMap<>();
    }

    public void reset() {
        this.contextMap = new HashMap<>();
        this.subscriptionMap = new HashMap<>();
    }

    public void createContext(FiwareCreateEntity context) {
        if(!contextMap.containsKey(context.id)) {
            contextMap.put(context.id, context);
        }
    }

    public void addSubscription(FiwareSubscription subscription) {
        if(!subscriptionMap.containsKey(subscription)) {
            subscriptionMap.put(subscription.getId(), subscription);
        }
    }

    public FiwareEntity getContext(String id) {
        return contextMap.get(id);
    }

    public String getProcId() {
        return "BFTBrokerClient_" + procId;
    }

    public long getTimestamp() {
        return System.currentTimeMillis()/100;
    }

    public String execute(String req) {
        //DataMessage dm = new DataMessage("rest_client_" + this.procId, this.opCount++, System.currentTimeMillis()/100, "\""+req.replace("\'", "|")+"\"");
        //byte[] resp = proxy.invokeOrdered((dm.toString().getBytes(StandardCharsets.UTF_8)));
        byte[] resp = proxy.invokeOrdered(req.getBytes(StandardCharsets.UTF_8));
        return new String(resp);
    }

    public static JSONObject createJSONObj(String procId, long timestamp, String objectId, String operation, JSONObject payload, String service) {
        JSONObject ret = new JSONObject();
        ret.put("procId", procId);
        ret.put("timestamp", timestamp);
        ret.put("object", objectId);
        ret.put("bft_iot/operation", operation);
        ret.put("value", payload);
        try {
            ret.put("service", service);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(ret);
        return ret;
    }

    static void notify(String uri, FiwareEntity fe) {
        try {
            URL url = new URL(uri);
            System.out.println(url.toString());
            URLConnection con = url.openConnection();
            byte[] contents = fe.toString().replace("'","\"").getBytes(StandardCharsets.UTF_8);
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.addRequestProperty("Content-Type","application/json");
            http.addRequestProperty("Content-Length", ""+ contents.length);
            http.setDoOutput(true);
            http.connect();
            OutputStream os = http.getOutputStream();
            os.write(contents);
            os.flush();
            os.close();
            System.out.println(String.valueOf(http.getResponseCode()));
            System.out.println(http.getResponseMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ... BFT_REST_Proxy <process id> <port number>");
            System.out.println("       process id: process identifier corresponds directly with ServiceProxy id");
            System.out.println("       port number: http port number for incoming connections");
            System.exit(-1);
        }

        int procId = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);

        try {
            BFT_Broker_Client client = new BFT_Broker_Client(procId); //port not in use

            port(port);

            LinkedList<String> notificationUrls = new LinkedList<>();
            System.out.println("Starting HTTP server");

            Gson gson = new Gson();

            get("/", (req, res) -> {
                System.out.println("GET /");
                return "Hello World";
            });

            get("/hello", (req, res) -> {
                return "Hello World";
            });

            get("/reset", (req, res) -> {
                client.reset();
                return 200;
            });

            post("/subscribe", (req, res) -> {
                String url = req.body();
                System.out.println(url);
                notificationUrls.addLast(url);
                return 200;
            });

            get("/data", (req, res) -> {
                String ret = "{\"data\":[";
                for (String key : client.contextMap.keySet()) {
                    ret += String.format("%s,", client.contextMap.get(key).toString());
                }
                ret = ret.substring(0, ret.length() - 1);
                ret += "]}";
                ret = ret.replace("'", "\"");
                return ret;
            });

            path("/v2", () -> {

                path("/subscriptions", () -> {

                    // POST /v2/subscriptions
                    post("", (req, res) -> { // create subscription
                        System.out.println("POST /v2/subscriptions");

                        String service = req.params("Fiware-Service");

                        JSONObject jsonObj = new JSONObject(req.body());
                        JSONObject jsonRequest = createJSONObj(client.getProcId(), client.getTimestamp(), "", "subscription", jsonObj, service);

                        System.out.println(jsonRequest);
                        System.out.println("---------- BFT Quorum ----------");
                        String response = client.execute(jsonRequest.toString());
                        System.out.println(response);
                        System.out.println("---------- BFT Quorum (END) ----------");

                        try {
                            FiwareSubscription cS = gson.fromJson(req.body(), FiwareSubscription.class);
                            System.out.println(cS.toString());
                            client.addSubscription(cS);
                            res.status(201);
                            return "Created";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        res.status(400);
                        return "Not Created";
                    });
                });

                path("/entities", () -> {

                    // POST /v2/entities
                    post("", (req, res) -> { // create entity
                        System.out.println("POST /v2/entities");
                        String service = req.params("Fiware-Service");
                        JSONObject jsonObj = new JSONObject(req.body());
                        JSONObject jsonRequest = createJSONObj(client.getProcId(), client.getTimestamp(), "", "create", jsonObj, service);

                        System.out.println("---------- BFT Quorum ----------");
                        String response = client.execute(jsonRequest.toString());
                        System.out.println(response);
                        System.out.println("---------- BFT Quorum (END) ----------");

                        if (req.body().contains("trash_sensor")) {
                            TrashSensorEntity fe = gson.fromJson(req.body(), TrashSensorEntity.class);
                            System.out.println(fe);
                            client.createContext(fe);

                            for (String url : notificationUrls) {
                                notify(url, fe);
                            }
                        } else if (req.body().contains("humidity_sensor")) {
                            HumiditySensorEntity fe = gson.fromJson(req.body(), HumiditySensorEntity.class);
                            System.out.println(fe);
                            client.createContext(fe);
                            for (String url : notificationUrls) {
                                notify(url, fe);
                            }
                        } else if (req.body().contains("parking_sensor")) {

                        } else {

                        }
                        res.status(201);
                        return "Created";
                    });

                    // PATCH /v2/entities/{id}/attrs
                    patch("/:id/attrs", (req, res) -> { // update entity
                        System.out.println("PATCH /v2/entities/" + req.params(":id") + "/attrs");
                        FiwareEntity fe = client.getContext(req.params(":id"));
                        if (fe != null) {
                            String service = req.params("Fiware-Service");
                            JSONObject jsonObj = new JSONObject(req.body());
                            JSONObject jsonReq = createJSONObj(client.getProcId(), client.getTimestamp(), req.params(":id"), "update", jsonObj, service);

                            System.out.println("---------- BFT Quorum (START) ----------");
                            String resp = client.execute(jsonReq.toString());
                            System.out.println(resp);
                            System.out.println("---------- BFT Quorum (END) ----------");

                            if (fe instanceof TrashSensorEntity) {
                                float value = jsonObj.getJSONObject("ratio").getFloat("value");
                                ((TrashSensorEntity) fe).getRatio().setValue(value);
                                for (String url : notificationUrls) {
                                    notify(url, fe);
                                }
                            } else if (fe instanceof HumiditySensorEntity) {
                                float temp_value = jsonObj.getJSONObject("temperature").getFloat("value");
                                float humid_value = jsonObj.getJSONObject("humidity").getFloat("value");
                                ((HumiditySensorEntity) fe).getTemperature().setTemperature(temp_value);
                                ((HumiditySensorEntity) fe).getHumidity().setHumidity(humid_value);
                                for (String url : notificationUrls) {
                                    notify(url, fe);
                                }
                            }
                            res.status(204);
                            return "204 No Content";

                        }

                        res.status(404);
                        return "404 Not Found";
                    });

                    // GET /v2/entities/{id}
                    get("/:id", (req, res) -> { // query entity
                        try {
                            System.out.println("GET /v2/entities/" + req.params(":id"));
                            FiwareEntity fe = client.getContext(req.params(":id"));
                            if (fe != null) {
                                res.header("Content-Type", "application/json");
                                res.header("Content-Length", "" + fe.toString().getBytes(StandardCharsets.UTF_8).length);
                                res.status(200);
                                return fe.toString().replace("'", "\"");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        res.status(404);
                        return "Not Found";
                    });

                    // GET /v2/entities
                    get("/", (req, res) -> {
                        String ret = "[";
                        for (FiwareEntity fw : client.contextMap.values()) {
                            ret += fw.toString() + ",";
                        }
                        if (ret.endsWith(","))
                            ret = ret.substring(0, ret.length() - 1);
                        ret += "]";

                        System.out.println("GET /v2/entities");
                        System.out.println(ret);

                        res.header("Content-Type", "application/json");
                        res.header("Content-Length", "" + ret.getBytes(StandardCharsets.UTF_8).length);
                        res.status(200);
                        return ret;
                    });
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
