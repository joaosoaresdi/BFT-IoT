package bft_iot.json;

import java.util.Iterator;
import java.util.LinkedList;

public class JSArrayObject extends JSElement {
    public LinkedList<JSElement> elems = new LinkedList<>();

    @Override
    public int size() {
        return elems.size();
    }

    public boolean addLast(JSElement elem) {
        if(closed)
            return false;

        if(elems.size() == 0) {
            elems.addLast(elem);
            return true;
        }
        Iterator<JSElement> it = elems.descendingIterator();
        while(it.hasNext()) {
            JSElement crt = it.next();
            if(crt.addLast(elem))
                return true;
        }
        elems.addLast(elem);
        return true;
    }

    public boolean closeLastObject() {
        if(closed) {
            return false;
        }
        Iterator<JSElement> it = elems.descendingIterator();
        while(it.hasNext()) {
            JSElement crt = it.next();
            if(!(crt instanceof JSArrayObject)) {
                if (crt.closeLastObject()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean closeLastArray() {
        if(closed) {
            return false;
        }

        Iterator<JSElement> it = elems.descendingIterator();
        while(it.hasNext()) {
            JSElement crt = it.next();
            if(crt instanceof JSArrayObject) {
//                System.out.println("------------------------> CLOSING ARRAY");
//                System.out.println("------------> " + crt.getClass() + " : "  + (crt.closed?"CLOSED":"OPEN")  + " : " + crt);
//                System.out.println("------------------------");
                if (crt.closeLastArray()) {
                    return true;
                }
            }
        }
//        System.out.println("------------------------> CLOSING ARRAY");
//        System.out.println("------------> " + this.getClass() + " : "  + (this.closed?"CLOSED":"OPEN")  + " : " + this);
//        System.out.println("------------------------");
        this.close();
        return true;
    }

    public String toString() {
        String res = "[";
        for(JSElement e : elems) {
            res += e.toString()+ ",";
        }
        if(res.endsWith(","))
            res = res.substring(0, res.length()-1);
        res += "]";
        return res;
    }
}
