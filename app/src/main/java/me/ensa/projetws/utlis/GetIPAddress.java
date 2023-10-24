package me.ensa.projetws.utlis;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GetIPAddress {
    public static String getIP() {
        try {
            InetAddress myIP = InetAddress.getLocalHost();
            return myIP.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}

