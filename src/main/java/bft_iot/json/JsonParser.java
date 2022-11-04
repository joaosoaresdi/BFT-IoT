package bft_iot.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.StringReader;

public class JsonParser {
    private static final Gson gson = new Gson();

    public static JSNestedObject parseJSON(String json) {
        JSNestedObject se = new JSNestedObject();

        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            parse(reader, se);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return se;
    }

    private static void parse(JsonReader reader, JSElement se) throws IOException {
//        System.out.println("");
        JsonToken token = reader.peek();
        if (token.equals(JsonToken.BEGIN_OBJECT)) {
            //System.out.println("{");

            reader.beginObject();
            if(se.size() != 0) {
                se.addLast(new JSNestedObject());
            }
            parse(reader, se);
        } else if (token.equals(JsonToken.END_OBJECT)) {
            //System.out.println("}");
            reader.endObject();
//            System.out.println("CLOSE LAST OBJECT");
            se.closeLastObject();
            parse(reader, se);
        } else if (token.equals(JsonToken.BEGIN_ARRAY)) {
            //System.out.println("[");

            reader.beginArray();
            se.addLast(new JSArrayObject());
            handleArray(reader, se);
        } else if (token.equals(JsonToken.END_ARRAY)) {
            //System.out.println("]");

            reader.endArray();
//            System.out.println("CLOSE LAST ARRAY");
            se.closeLastArray();
            parse(reader, se);
        } else if (token.equals(JsonToken.END_DOCUMENT)) {
//            System.out.println("END DOCUMENT END DOCUMENT END DOCUMENT END DOCUMENT END DOCUMENT END DOCUMENT ");
            reader.close();
            se.close();
        } else {
            handleNonArrayToken(reader, token, se);
        }
//        }
    }

    private static void handleNonArrayToken(JsonReader reader, JsonToken token, JSElement se) throws IOException {
        if (token.equals(JsonToken.NAME)) {
            String key = reader.nextName();
            se.addLast(new JSKeyValue(key));
            //System.out.println("KEY : " + key);
        }
        else if (token.equals(JsonToken.STRING)) {
            String value = reader.nextString();
            se.addLast(new JSValue(value));
            //System.out.println("VALUE : " + value);
        }
        else if (token.equals(JsonToken.NUMBER))
            System.out.println(reader.nextDouble());
        else if (token.equals(JsonToken.BOOLEAN))
            System.out.println(reader.nextBoolean());
        else {
        }
        parse(reader, se);
    }

    private static void handleArray(JsonReader reader, JSElement se) throws IOException {
        JsonToken token = reader.peek();
        if (token.equals(JsonToken.END_ARRAY)) {
            reader.endArray();
            parse(reader, se);
        } else if (token.equals(JsonToken.BEGIN_ARRAY)) {
            reader.beginArray();
            handleArray(reader, se);
        } else if (token.equals(JsonToken.BEGIN_OBJECT)) {
            parse(reader, se);
        } else if (token.equals(JsonToken.END_OBJECT)) {
            System.out.println("CLOSE LAST OBJECT");
            se.closeLastObject();
            reader.endObject();
        } else {
            handleNonArrayToken(reader, token, se);
        }
    }

    public static void main(String[] args) {
        String json = "{\"description\":\"Subscription from trash to trash_sensor sensors on location ratio\",\"subject\":{\"entities\":[{\"idPattern\":\".*\",\"type\":\"trash_sensor\"}],\"condition\":{\"attrs\":[\"location\",\"ratio\"]}},\"notification\":{\"http\":{\"url\":\"http://host.docker.internal:8080/trash/notify\"},\"attrs\":[\"location\",\"ratio\"]},\"expires\":\"2040-01-01T14:00:00.00Z\"}";

        JsonReader reader = new JsonReader(new StringReader(json));

        try {
            JSNestedObject se = new JSNestedObject();
            parse(reader, se);
            System.out.println(json);

            System.out.println(se);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
