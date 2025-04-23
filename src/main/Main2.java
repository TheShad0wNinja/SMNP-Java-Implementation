package main;

import model.Message;
import util.ConnectionUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Main2 {
    public static void main(String[] args) {
        ConnectionUtil cu =  new ConnectionUtil(100);
        Message str = cu.receiveMessage();
        DataOutputStream out = new DataOutputStream();
        out.write();
        System.out.println(str);
    }
}
