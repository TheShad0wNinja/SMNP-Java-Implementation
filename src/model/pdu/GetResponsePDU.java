package model.pdu;

import java.util.List;

public class GetResponsePDU extends PDU{
    public GetResponsePDU(int requestId, List<VarBind> variableBindings) {
        super(requestId, variableBindings);
    }
}
