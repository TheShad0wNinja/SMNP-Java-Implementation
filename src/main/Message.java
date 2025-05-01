package main;

import java.io.Serializable;

public record Message(String community, main.Message.PDUType pduType, String oid,
                      String value) implements Serializable {
    public enum PDUType {
        GET_REQUEST,
        GET_NEXT,
        GET_RESPONSE,
        SET_REQUEST,
        TRAP
    }

    @Override
    public String toString() {
        return String.format("SNMP Message [Community: %s, PDU: %s, OID: %s, Value: %s]",
                community, pduType, oid, value);
    }
} 