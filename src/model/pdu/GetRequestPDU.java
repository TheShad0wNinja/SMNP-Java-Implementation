package model.pdu;

import java.util.List;

public class GetRequestPDU extends PDU {
    public GetRequestPDU(int requestId, List<VarBind> variableBindings) {
        super(requestId, variableBindings);
    }
}
