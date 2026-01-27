package net.altosheeve.tracking.client.Networking;

import com.google.common.primitives.Bytes;
import net.altosheeve.tracking.client.Core.Relaying;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class UDPClient {

    public static DatagramSocket socket;
    public static InetAddress ip;
    public static int port;

    public static final int packetLength = 1024;

    private static byte[] out = new byte[]{};

    public interface MessageCallback {
        void cb(UDPObject udpObject);
    }

    public static class ListeningThread extends Thread {
        private final MessageCallback cb;
        private final DatagramSocket socket;

        public ListeningThread(MessageCallback cb, DatagramSocket socket) {
            this.cb = cb;
            this.socket = socket;
        }

        public void run() {

            while (true) {
                byte[] receive = new byte[packetLength];
                DatagramPacket packet = new DatagramPacket(receive, packetLength);

                try {
                    this.socket.receive(packet);
                } catch (IOException e) {
                    break;
                }

                Iterator<Byte> bytes = Bytes.asList(receive).iterator();

                while (bytes.hasNext()) {
                    int identifier = bytes.next();

                    if (identifier == 0) break;

                    int length = bytes.next();

                    ArrayList<Byte> body = new ArrayList<>();

                    for (int i = 0; i < length && bytes.hasNext(); i++) body.add(bytes.next());

                    this.cb.cb(new UDPObject(identifier, Bytes.toArray(body)));
                }

            }

            while (true) {
                int tries = 0;
                try {
                    createConnection(ip.getHostName(), port);
                    System.out.println("reconnect successful");
                    break;
                } catch (SocketException | UnknownHostException e) {
                    tries++;
                    System.out.println("reconnect failed with " + tries + " tries");
                }
            }
        }
    }

    public static void createConnection(String uri, int p) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        ip = InetAddress.getByName(uri);
        port = p;
    }

    public static void sendData(byte[] message) throws IOException {
        byte[] out = new byte[packetLength];

        System.arraycopy(message, 0, out, 0, message.length);

        DatagramPacket packet = new DatagramPacket(out, packetLength, ip, port);
        socket.send(packet);
    }

    public static void queueObject(UDPObject data) throws IOException {
        byte[] original = out;

        byte[] head = new byte[]{(byte) data.identifier, (byte) data.length};
        byte[] combine = TypeGenerators.combineBuffers(head, Bytes.toArray(data.data));

        if (out.length + combine.length < packetLength) {
            out = TypeGenerators.combineBuffers(original, combine);
            return;
        }

        sendData(out);
        out = combine;
    }

    public static void pushQueue() throws IOException {
        sendData(out);
        out = new byte[]{};
    }

    public static void listen(MessageCallback cb) {
        System.out.println("Soprano: UDP client now listening");
        ListeningThread listener = new ListeningThread(cb, socket);
        listener.start();
    }

    public static void main(String[] args) throws IOException {
        createConnection(Relaying.host, Relaying.port);
    }

}
