package main;

import model.Message;
import model.pdu.GetRequestPDU;
import model.pdu.VarBind;
import util.ConnectionUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        ConnectionUtil cu =  new ConnectionUtil();
        List<VarBind> variableBindings = List.of(new VarBind("discoverMe", null));
        Message msg = new Message("public", new GetRequestPDU(0, variableBindings));
        cu.sendMessage(msg, InetAddress.getLocalHost(), 100);
    }

}
