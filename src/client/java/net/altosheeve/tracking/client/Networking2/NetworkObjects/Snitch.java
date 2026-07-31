package net.altosheeve.tracking.client.Networking2.NetworkObjects;

import net.altosheeve.tracking.client.Networking2.Connection;
import net.altosheeve.tracking.client.Networking2.Typing;

import java.util.Iterator;

public class Snitch extends NetworkObject {

    public String username;

    public float x;
    public float y;
    public float z;

    public boolean alert;

    public String group;
    public String room;

    public Snitch(Iterator<Byte> bytes, NetworkObject incubator) {

        super(new Connection(), 1);

        this.username = Typing.decodeString(bytes);

        this.x = Typing.decodeFloat(bytes);
        this.y = Typing.decodeFloat(bytes);
        this.z = Typing.decodeFloat(bytes);

        this.alert = Typing.decodeInt(bytes) == 1;

        this.room  = Typing.decodeString(bytes);
        this.group = Typing.decodeString(bytes);

        this.identifier = 2;
        this.uuid = incubator.uuid;
        this.timestamp = incubator.timestamp;

        this.origin = incubator.origin;
        this.method = incubator.method;

        this.data = generateOut();

    }

    public Snitch(String uuid, String username, float x, float y, float z, boolean alert, String room, String group) {

        super(null, 1);

        this.uuid = uuid;
        this.username = username;

        this.x = x;
        this.y = y;
        this.z = z;

        this.alert = alert;

        this.room  = room;
        this.group = group;

        this.data = generateOut();

    }

    @Override
    public byte[] generateOut() {

        byte[] username = Typing.encodeString(this.username);

        byte[] x = Typing.encodeFloat(this.x);
        byte[] y = Typing.encodeFloat(this.y);
        byte[] z = Typing.encodeFloat(this.z);

        //TODO: change this!! you should make an actual boolean type instead of using ints
        byte[] alert;
        if (this.alert) alert = Typing.encodeInt(1);
        else            alert = Typing.encodeInt(0);

        byte[] room  = Typing.encodeString(this.room);
        byte[] group = Typing.encodeString(this.group);

        byte[] body = Typing.combineBuffers(username, x, y, z, alert, room, group);

        return createFullObjectData(2, this.uuid, body, this.timestamp);

    }
}
