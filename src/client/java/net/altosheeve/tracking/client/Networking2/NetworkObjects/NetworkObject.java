package net.altosheeve.tracking.client.Networking2.NetworkObjects;

import net.altosheeve.tracking.client.Networking2.Connection;
import net.altosheeve.tracking.client.Networking2.Typing;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class NetworkObject {
    public int identifier;
    public byte[] data;
    public long timestamp;
    public Connection origin;
    public int method;
    public String uuid;

    public NetworkObject(Connection origin, int method) {
        this.origin = origin;
        this.method = method;
        this.timestamp = new Date().getTime();
    }

    public static ArrayList<NetworkObject> parseNetworkObjects(Iterator<Byte> bytes, Connection origin, int method) {

        boolean stop = false;
        ArrayList<NetworkObject> out = new ArrayList<>();

        while (bytes.hasNext() && !stop) {

            NetworkObject newNetworkObject = new NetworkObject(origin, method);
            newNetworkObject.readHeader(bytes);

            switch (newNetworkObject.identifier) {

                case 0 : //stop case
                    stop = true;
                    break;

                case 1 :
                    Player newPlayer = new Player(bytes, newNetworkObject);
                    out.add(newPlayer);
                    break;

                case 2 :
                    Snitch newSnitch = new Snitch(bytes, newNetworkObject);
                    out.add(newSnitch);
                    break;

            }

        }

        return out;

    }

    public static byte[] createFullObjectData(int identifier, String uuid, byte[] body, long timestamp) {

        byte[] identiferBytes = new byte[]{(byte) identifier};
        byte[] uuidBytes = Typing.encodeString(uuid);
        byte[] tmBytes = Typing.encodeLong(timestamp);

        return Typing.combineBuffers(identiferBytes, uuidBytes, tmBytes, body);

    }


    public void readHeader(Iterator<Byte> bytes) {

        this.identifier = bytes.next();
        this.uuid = Typing.decodeString(bytes);
        this.timestamp = Typing.decodeLong(bytes);

    }

    public byte[] generateOut() {
        return new byte[0];
    }
}
