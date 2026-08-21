package net.altosheeve.tracking.client.Networking2;

import net.altosheeve.tracking.client.Networking2.NetworkObjects.NetworkObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Relay {

    public static ArrayList<NetworkObject> globalObjectsUnsynced = new ArrayList<>();
    public static List<NetworkObject> globalObjects = Collections.synchronizedList(globalObjectsUnsynced);
    public static ArrayList<Connection> connections = new ArrayList<>();

    public static int HTTPPort = 9000;
    public static int TCPport = 9001;
    public static int UDPport = 9002;

    public static String oppwatchHost = "170.187.207.133";

    public static UDPhelper udpListener;

    public static void listen() throws IOException {

        RestServer restServer = new RestServer(HTTPPort);

        udpListener = new UDPhelper(oppwatchHost, UDPport);

        udpListener.startRecieving(Relay::addGlobalObject);

        TCPHelper.startServer(Relay::addGlobalObject);

        restServer.start();

    }

    public static void run() throws IOException {

        while (true) {

            if (!globalObjects.isEmpty()) {

                ArrayList<NetworkObject> remove = new ArrayList<>();

                for (NetworkObject networkObject : globalObjects) {
                    if (networkObject.method == 0) udpListener.queueObject(networkObject);
                    if (networkObject.method == 1) {

                        for (Connection connection : connections) {

                            connection.TCPsenderClient.send(networkObject.generateOut());

                            remove.add(networkObject);

                        }

                    }
                }

                for (Connection connection : connections) {

                    udpListener.send(connection.host);

                }

                udpListener.clearQueue();

                globalObjects.removeAll(remove);

            }

        }

    }

    public static void addGlobalObject(NetworkObject object) {

        System.out.println(object.uuid);

        for (NetworkObject globalObject : globalObjects) {

            if (Objects.equals(globalObject.uuid, object.uuid)) {

                globalObjects.remove(globalObject);
                break;

            }

        }

        globalObjects.add(object);

    }

    public static Connection getConnection(String host) {

        for (Connection connection : connections) {

            System.out.println(connection.host);
            System.out.println(host);

            System.out.println(Objects.equals(connection.host, host));

            if (Objects.equals(connection.host, host)) return connection;

        }
        return null;

    }

}
