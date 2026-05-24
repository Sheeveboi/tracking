package net.altosheeve.tracking.client.Networking;

import com.google.common.primitives.Bytes;

import java.util.*;

public class UDPObject {
    public int identifier;
    public int length;
    public ArrayList<Byte> data;

    public UDPObject(int identifier,byte[] data) {
        this.identifier = identifier;

        this.data = new ArrayList<>(Bytes.asList(data));
        this.length = data.length;
    }
}
