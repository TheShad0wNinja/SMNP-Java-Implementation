package model;

import java.io.Serializable;
import model.pdu.*;

public class Message implements Serializable {
    private String community;
    private PDU pdu;

    public Message(String community, PDU pdu) {
        this.community = community;
        this.pdu = pdu;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public PDU getPdu() {
        return pdu;
    }

    public void setPdu(PDU pdu) {
        this.pdu = pdu;
    }

    @Override
    public String toString() {
        return "Message{" +
                "community='" + community + '\'' +
                ", pdu=" + pdu +
                '}';
    }
}
