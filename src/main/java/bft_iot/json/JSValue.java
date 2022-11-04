package bft_iot.json;

public class JSValue extends JSElement {
    private String value;

    public JSValue(String val) {
        this.value = val;
        this.closed = true;
    }

    public String toString() {
        return "'"+value+"'";
    }

    @Override
    public int size() {
        return 0;
    }

    public boolean addLast(JSElement elem) {
        return false;
    }

    public boolean closeLastObject() {
        return false;
    }

    public boolean closeLastArray() {
        return false;
    }
}
