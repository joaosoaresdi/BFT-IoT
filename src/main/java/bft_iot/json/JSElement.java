package bft_iot.json;

public abstract class JSElement {
    protected boolean closed = false;

    public abstract int size();
    public abstract boolean addLast(JSElement elem);
    public abstract boolean closeLastObject();
    public abstract boolean closeLastArray();

    public final boolean isClosed() {
        return closed;
    }

    public final void close() {
//        System.out.println("------------------------> CLOSING ");
//        System.out.println("------------> " + this.getClass() + " : "  + (this.closed?"CLOSED":"OPEN")  + " : " + this);
//        System.out.println("------------------------");

        closed = true;
    }

}
