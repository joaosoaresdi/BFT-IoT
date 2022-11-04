package bft_iot.json.entities;

import java.util.concurrent.atomic.AtomicLong;

public class FiwareSubscription extends FiwareEntity {
    private static final AtomicLong counter = new AtomicLong();

    String id = null;
    String description;
    Subject subject;
    Notification notification;
    String expires;

    public String toString() {
        return String.format("{'description':'%s', 'subject':%s, 'notification':%s, 'expires':'%s'}", description, subject.toString(), notification.toString(), expires);
    }

    public String getId() {
        if(id == null)
            id = "Subscription_" + counter.getAndIncrement();
        return id;
    }

    @Override
    public boolean compareTo(FiwareEntity other, BFTIoTConfig config) {
        return true;
    }
}

class Subject {
    Entity[] entities;
    Condition condition;

    public String toString() {
        String entities_str = "[";
        for (Entity e : entities) {
            entities_str += "'"+e.toString() + "',";
        }
        entities_str = entities_str.substring(0, entities_str.length()-1);
        entities_str += "]";
        return String.format("{'entities':%s, 'condition':%s}", entities_str, condition.toString());
    }

}

class Entity {
    String idPattern;
    String type;

    public String toString() {
        return String.format("{'idPattern':'%s', 'type':'%s'}", idPattern, type);
    }
}

class Condition {
    String[] attrs;

    public String toString() {
        String attrs_str = "[";
        for (String s : attrs) {
            attrs_str += "'" + s + "',";
        }
        attrs_str = attrs_str.substring(0, attrs_str.length()-1);
        attrs_str += "]";
        return String.format("{'attrs':%s}", attrs_str);
    }
}

class Notification {
    Http http;
    String[] attrs;

    public String toString() {
        String attrs_str = "[";
        for (String s : attrs) {
            attrs_str += "'" + s + "',";
        }
        attrs_str = attrs_str.substring(0, attrs_str.length()-1);
        attrs_str += "]";
        return String.format("{'http':%s, 'attrs':%s}", http.toString(), attrs_str);
    }
}

class Http {
    String url;

    public String toString() {
        return String.format("{'url':'%s'}", url);
    }
}

/*
{
    "description":"Subscription from trash to trash_sensor sensors on location,ratio",
    "subject": {
        "entities":
            [
                {
                    "idPattern":".*" ,
                    "type":"trash_sensor"}
            ],
        "condition": {
            "attrs":
            [
                "location","ratio"
            ]
        }
    },
    "notification": {
        "http": {
            "url":"http://host.docker.internal:8080/trash/notify"
        },
        "attrs":
            ["location","ratio"]
        },
    "expires":"2040-01-01T14:00:00.00Z"
}
 */

//{"description":"Subscription from trash to trash_sensor sensors on location,ratio","subject":{"entities":[{"idPattern":".*","type":"trash_sensor"}],"condition":{"attrs":["location","ratio"]}},"notification":{"http":{"url":"http://host.docker.internal:8080/trash/notify"},"attrs":["location","ratio"]},"expires":"2040-01-01T14:00:00.00Z"}
//{description:Subscription from trash to trash_sensor sensors on location,ratio, subject:{entities:[{idPattern:.*, type:trash_sensor},], condition:{attrs:[location,ratio,]}}, notification:{http:{url:http://host.docker.internal:8080/trash/notify}, attrs:[location,ratio,]},expires:2040-01-01T14:00:00.00Z}
