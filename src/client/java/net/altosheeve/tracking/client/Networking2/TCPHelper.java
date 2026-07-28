package net.altosheeve.tracking.client.Networking2;

import net.altosheeve.tracking.client.Networking2.NetworkObjects.NetworkObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TCPHelper {

    public static final int packetLength = 1024;
    public static ServerSocket serverSocket;

    public BufferedReader in;
    public PrintWriter out;
    public Socket socket;

    private ArrayList<byte[]> nonSyncChunks = new ArrayList<>();
    private List<byte[]> chunks = Collections.synchronizedList(nonSyncChunks);

    public TCPHelper(BufferedReader in, PrintWriter out, Socket socket) {
        this.in = in;
        this.out = out;
        this.socket = socket;
    }

    public interface IncomingCallback {
        void cb(NetworkObject tcpObject) throws Exception;
    }

    //listens for messages from existing TCP connections
    public static class ConnectionThread extends Thread {

        private final IncomingCallback cb;
        private final Connection connection;

        public ConnectionThread(IncomingCallback cb, Connection connection) {
            this.cb = cb;
            this.connection = connection;
        }

        public void run() {

            while (true) {

                try {

                    byte[] message = Base64.getDecoder().decode(this.connection.TCPsenderClient.in.readLine());

                    Iterator<Byte> bytes = IntStream.range(0, message.length).
                            mapToObj(i -> message[i])
                            .collect(Collectors.toList())
                            .iterator();

                    ArrayList<NetworkObject> networkObjects = NetworkObject.parseNetworkObjects(bytes, this.connection, 1);

                    for (NetworkObject networkObject : networkObjects) {
                        try {
                            this.cb.cb(networkObject);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

        }

    }

    //listens for and generates new TCP connections
    public static class NewConnectionThread extends Thread {
        private final IncomingCallback cb;
        public NewConnectionThread(IncomingCallback cb) {
            this.cb = cb;
        }

        public void run() {

            while (true) {

                try {

                    Socket clientSocket = serverSocket.accept();

                    Connection connection = Relay.getConnection(clientSocket.getInetAddress().getHostAddress());

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    assert connection != null;
                    connection.TCPsenderClient = new TCPHelper(in, out, clientSocket);

                    if (connection.UDPsenderClient != null) connection.fullyConnected = true;

                    ConnectionThread connectionThread = new ConnectionThread(this.cb, connection);
                    connectionThread.start();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        }
    }

    public void send(byte[] data) {

        String message = Base64.getEncoder().encodeToString(data);

        this.out.println(message);

    }

    public static void startServer(IncomingCallback cb) throws IOException {

        System.out.println("TCP listener now active");

        serverSocket = new ServerSocket(Relay.TCPport);
        NewConnectionThread listener = new NewConnectionThread(cb);
        listener.start();

    }

    public static TCPHelper startClient(IncomingCallback cb, String host, int port) throws IOException {

        System.out.println("TCP Client created");

        Connection newConnection = new Connection();

        Socket clientSocket = new Socket(host, port);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        newConnection.TCPsenderClient = new TCPHelper(in, out, clientSocket);

        ConnectionThread connectionThread = new ConnectionThread(cb, newConnection);
        connectionThread.start();

        return newConnection.TCPsenderClient;

    }

}
