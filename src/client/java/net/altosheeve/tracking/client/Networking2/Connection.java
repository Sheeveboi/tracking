package net.altosheeve.tracking.client.Networking2;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Connection {

    public UDPhelper UDPsenderClient;
    public TCPHelper TCPsenderClient;

    public String mcServerHost = "";
    public int dimension = 0;
    public String host;
    public int port;

    public boolean fullyConnected = false;

    public Connection() {}

    public Connection(String host, int port) {

        this.host = host;
        this.port = port;

    }

}
