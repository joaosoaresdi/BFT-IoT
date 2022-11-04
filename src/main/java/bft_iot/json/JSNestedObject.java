package bft_iot.json;

import java.util.Iterator;
import java.util.LinkedList;

public class JSNestedObject extends JSElement {

    public LinkedList<JSElement> elems = new LinkedList<>();

    @Override
    public int size() {
        return elems.size();
    }

    public boolean addLast(JSElement elem) {
//        System.out.println("------------------------> ADDLAST");
//        System.out.println("------------> " + elem.getClass() + " : " + elem);
//        System.out.println("------------> " + this.getClass() + " : "  + (this.closed?"CLOSED":"OPEN")  + " : " + this);
//        System.out.println("------------------------");

        if(closed) {
            return false;
        }
        if(elems.size() == 0) {
            elems.addLast(elem);
//            System.out.println("++++++++++++> " + this.getClass() + " : "  + (this.closed?"CLOSED":"OPEN")  + " : " + this);
            return true;
        }
        Iterator<JSElement> it = elems.descendingIterator();
        while(it.hasNext()) {
            JSElement crt = it.next();
//            System.out.println("????????????> " + crt.getClass() + " : "  + (crt.closed?"CLOSED":"OPEN") + " : " + crt);
            if(crt.addLast(elem)) {
//                System.out.println("++++++++++++> " + crt.getClass() + " : "  + (crt.closed?"CLOSED":"OPEN")  + " : " + crt);
                return true;
            }
            else {
//                System.out.println("------------> NOT INSERTED ");
            }
        }
        elems.addLast(elem);
//        System.out.println("++++++++++++> " + this.getClass() + " : "  + (this.closed?"CLOSED":"OPEN")  + " : " + this);
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
                if(crt.closeLastObject()) {
                    return true;
                }
            }
        }
//        System.out.println("------------------------> CLOSING OBJECT");
//        System.out.println("------------> " + this.getClass() + " : "  + (this.closed?"CLOSED":"OPEN")  + " : " + this);
//        System.out.println("------------------------");
        this.close();
        return true;
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
                else {
                    crt.close();
                    return true;
                }
            }
            if (crt.closeLastArray()) {
                return true;
            }
        }
        return false;
    }


    public String toString() {
        String res = "{";
        for(JSElement e : elems) {
            if(e == null) {
                res += "null ,";
            }
            else {
                res += e.toString() + ",";
            }
        }
        if(res.endsWith(","))
            res = res.substring(0, res.length()-1);
        res += "}";
        return res;
    }
}
