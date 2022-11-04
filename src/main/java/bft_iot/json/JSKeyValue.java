package bft_iot.json;

public class JSKeyValue extends JSElement {
    private String key;
    private JSElement value;

    public JSKeyValue(String key) {
        this.key = key;
        this.value = null;
    }

    @Override
    public int size() {
        return 0;
    }

    public boolean addLast(JSElement elem) {
        if(closed)
            return false;

        if(value == null) {
            value = elem;
            if(elem instanceof JSValue) {
                this.closed = true;
            }
            return true;
        } else if (!(value instanceof JSValue)) {
            return value.addLast(elem);
        }
        return false;
    }

    public boolean closeLastObject() {
        if(value.isClosed())
            return false;

        if(value instanceof JSValue) {
            this.close();
            return true;
        }
        else if (value.closeLastObject())
            return true;
        else if (value instanceof JSNestedObject) {
            value.close();
            return true;
        }
        return false;
    }

    public boolean closeLastArray() {
        if (value.isClosed())
            return false;

        if(value instanceof JSArrayObject) {
//            System.out.println("------------------------> CLOSING ARRAY");
//            System.out.println("------------> " + value.getClass() + " : "  + (value.closed?"CLOSED":"OPEN")  + " : " + value);
//            System.out.println("------------------------");
            if(value.closeLastArray()) {
                return true;
            }
            else {
                value.close();
            }
        } else {
            return value.closeLastArray();
        }
        return false;
    }

    public String toString() {
        return String.format("'%s':%s",key,(value==null?"null":value.toString()));
    }
}
