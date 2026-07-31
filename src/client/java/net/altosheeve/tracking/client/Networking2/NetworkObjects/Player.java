package net.altosheeve.tracking.client.Networking2.NetworkObjects;

import net.altosheeve.tracking.client.Networking.Request;
import net.altosheeve.tracking.client.Networking.Verification;
import net.altosheeve.tracking.client.Networking2.Connection;
import net.altosheeve.tracking.client.Networking2.Typing;
import net.altosheeve.tracking.client.Waypoints.Waypoint;
import net.minecraft.entity.Entity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class Player extends NetworkObject {

    public String username;

    public float health;

    public float x;
    public float y;
    public float z;

    public Player(Iterator<Byte> bytes, NetworkObject incubator) {

        super(new Connection(), 0);

        this.username = Typing.decodeString(bytes);

        this.x = Typing.decodeFloat(bytes);
        this.y = Typing.decodeFloat(bytes);
        this.z = Typing.decodeFloat(bytes);

        this.health = Typing.decodeFloat(bytes);

        this.identifier = 1;
        this.uuid = incubator.uuid;
        this.timestamp = incubator.timestamp;

        this.origin = incubator.origin;
        this.method = incubator.method;

        this.data = generateOut();

    }

    public Player(String uuid, String username, float x, float y, float z, float health) {

        super(null, 0);

        this.uuid = uuid;
        this.username = username;
        this.x = x;
        this.y = y;
        this.z = z;
        this.health = health;

        this.method = 0;

        this.data = generateOut();

    }

    public int getThreat() {

        if (!Waypoint.availableThreatLevels.containsKey(this.uuid)) {

            String headers = "{" +
                    "'Function' : 'get_player'," +
                    "'Database' : 'oppwatch'" +
                    "}";

            String body = "{" +
                    "'username' : '" + this.username + "'" +
                    "}";

            Request.get("http://" + Verification.caligulaEndpoint + "/functions", headers, body, (response) -> {

                if (response.statusCode() == 200) {

                    JSONObject playerJson = new JSONObject(response.body());

                    JSONArray playerDataAg = playerJson.getJSONArray("data");

                    if (!playerDataAg.isEmpty()) {

                        JSONArray playerData = playerDataAg.getJSONArray(0);
                        int threat = playerData.getInt(1);

                        Waypoint.availableThreatLevels.put(this.uuid, threat);

                    }

                    else Waypoint.availableThreatLevels.put(this.uuid, 1);

                }

                else Waypoint.availableThreatLevels.put(this.uuid, 1);

            });

            Waypoint.availableThreatLevels.put(this.uuid, 1);

        }

        return Waypoint.availableThreatLevels.get(this.uuid);

    }

    @Override
    public byte[] generateOut() {

        byte[] username = Typing.encodeString(this.username);
        byte[] x = Typing.encodeFloat(this.x);
        byte[] y = Typing.encodeFloat(this.y);
        byte[] z = Typing.encodeFloat(this.z);

        byte[] health = Typing.encodeFloat(this.health);

        byte[] body = Typing.combineBuffers(username, x, y, z, health);

        return createFullObjectData(1, this.uuid, body, this.timestamp);

    }
}
