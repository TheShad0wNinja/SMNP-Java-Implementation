package main;

import java.io.Serializable;

public class Message implements Serializable {
    private String community;  // Community string
    private PDUType pduType;  // Type of PDU
    private String oid;  // Object Identifier
    private String value;  // Value associated with the OID

    public enum PDUType {
        GET_REQUEST,
        GET_NEXT,
        GET_RESPONSE,
        SET_REQUEST,
        TRAP
    }

    public Message(String community, PDUType pduType, String oid, String value) {
        this.community = community;
        this.pduType = pduType;
        this.oid = oid;
        this.value = value;
    }

    // Getters and setters
    public String getCommunity() { return community; }
    public PDUType getPduType() { return pduType; }
    public String getOid() { return oid; }
    public String getValue() { return value; }

    @Override
    public String toString() {
        return String.format("SNMP Message [Community: %s, PDU: %s, OID: %s, Value: %s]",
                community, pduType, oid, value);
    }
} 