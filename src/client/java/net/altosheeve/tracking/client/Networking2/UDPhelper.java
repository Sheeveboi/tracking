package net.altosheeve.tracking.client.Networking2;

import net.altosheeve.tracking.client.Core.Debug;
import net.altosheeve.tracking.client.Networking2.NetworkObjects.NetworkObject;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UDPhelper {

    public DatagramSocket socket;
    public InetAddress    ip;
    public int            port;

    private ArrayList<byte[]> nonSyncChunks = new ArrayList<>();
    private List<byte[]> chunks = Collections.synchronizedList(nonSyncChunks);
    public  boolean sending = true;

    public static final int packetLength = 1024;

    public interface ObjectCallback {
        void cb(NetworkObject udpObject) throws Exception;
    }

    public static class ListeningThread extends Thread {
        private final ObjectCallback cb;
        private final UDPhelper client;

        public ListeningThread(ObjectCallback cb, UDPhelper client) {
            this.cb = cb;
            this.client = client;
        }

        public void run() {

            while (true) {

                byte[] receive = new byte[packetLength];
                DatagramPacket packet = new DatagramPacket(receive, packetLength);

                try {
                    this.client.socket.receive(packet);
                } catch (IOException e) {
                    break;
                }

                Iterator<Byte> bytes = IntStream.range(0, receive.length).
                        mapToObj(i -> receive[i])
                        .collect(Collectors.toList())
                        .iterator();

                ArrayList<NetworkObject> networkObjects = NetworkObject.parseNetworkObjects(bytes, Relay.getConnection(packet.getAddress().getHostAddress()), 0);

                Debug.UDPObjectCount = networkObjects.size();

                for (NetworkObject networkObject : networkObjects) {
                    try {
                        this.cb.cb(networkObject);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            while (true) {
                int tries = 0;
                try {
                    client.startRecieving(this.cb);
                    System.out.println("reconnect successful");
                    break;
                } catch (SocketException e) {
                    tries++;
                    System.out.println("reconnect failed with " + tries + " tries");
                }
            }
        }
    }

    public UDPhelper(String uri, int p) throws SocketException, UnknownHostException {
        System.out.println("building udp helper");
        this.ip = InetAddress.getByName(uri);
        this.port = p;
        this.chunks.add(new byte[]{});
        this.socket = new DatagramSocket(this.port);
    }

    public void send() {

        for (byte[] chunk : this.chunks) {

            if (chunk.length != 0) {

                byte[] out = new byte[packetLength];

                System.arraycopy(chunk, 0, out, 0, chunk.length);
                DatagramPacket packet = new DatagramPacket(out, packetLength, this.ip, this.port);

                this.sending = true;

                try {
                    socket.send(packet);
                } catch (IOException e) {
                    this.sending = false;
                }
            }
        }
    }

    public void send(String host) throws UnknownHostException {

        for (byte[] chunk : this.chunks) {

            if (chunk.length != 0) {

                byte[] out = new byte[packetLength];

                System.arraycopy(chunk, 0, out, 0, chunk.length);
                DatagramPacket packet = new DatagramPacket(out, packetLength, InetAddress.getByName(host), this.port);

                this.sending = true;

                try {
                    socket.send(packet);
                } catch (IOException e) {
                    this.sending = false;
                }
            }
        }
    }

    public void clearQueue() {
        this.chunks.clear();
        this.chunks.add(new byte[]{});
    }

    public void queueObject(NetworkObject data) throws IOException {

        byte[] currentChunk = this.chunks.getLast();
        byte[] push = data.generateOut();

        if (currentChunk.length + push.length < packetLength) {

            currentChunk = Typing.combineBuffers(currentChunk, push);
            this.chunks.remove(this.chunks.getLast());
            this.chunks.add(currentChunk);

        }

        else this.chunks.add(new byte[]{});

    }

    public void startRecieving(ObjectCallback cb) throws SocketException {

        System.out.println("UDP listener now active");
        ListeningThread listener = new ListeningThread(cb, this);
        listener.start();

    }

}
