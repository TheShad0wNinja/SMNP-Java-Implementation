package model.pdu;

import java.io.Serializable;
import java.util.List;

public abstract class PDU implements Serializable {
    protected final int requestId;
    protected boolean errorStatus;
    protected int errorIndex;
    protected final List<VarBind> variableBindings;

    public PDU(int requestId, List<VarBind> variableBindings) {
        this.requestId = requestId;
        this.variableBindings = variableBindings;
        this.errorStatus = false;
        this.errorIndex = -1;
    }

    public List<VarBind> getVariableBindings() {
        return variableBindings;
    }

    public void setError(int errorIndex) {
        this.errorStatus = true;
        if (errorIndex >= variableBindings.size())
            this.errorIndex = 0;
        else
            this.errorIndex = errorIndex;
    }

    public void addVariableBinding(VarBind variableBinding) {
        this.variableBindings.add(variableBinding);
    }

    @Override
    public String toString() {
        return "PDU{" +
                "requestId=" + requestId +
                ", errorStatus=" + errorStatus +
                ", errorIndex=" + errorIndex +
                ", variableBindings=" + variableBindings +
                '}';
    }
}
