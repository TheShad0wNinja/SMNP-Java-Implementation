package model.pdu;

import java.io.Serializable;

public record VarBind(String oid, Object value) implements Serializable {

    @Override
    public String toString() {
        return "VarBind{" +
                "oid='" + oid + '\'' +
                ", value=" + value +
                '}';
    }
}
