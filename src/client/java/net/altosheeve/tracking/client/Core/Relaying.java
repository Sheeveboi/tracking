package net.altosheeve.tracking.client.Core;

import net.altosheeve.tracking.client.Networking.*;
import net.altosheeve.tracking.client.Networking2.NetworkObjects.NetworkObject;
import net.altosheeve.tracking.client.Networking2.NetworkObjects.Player;
import net.altosheeve.tracking.client.Networking2.Relay;
import net.altosheeve.tracking.client.Networking2.UDPhelper;
import net.altosheeve.tracking.client.Waypoints.Waypoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.time.Clock;

public class Relaying {

    public static String host = "170.187.207.133";
    public static int port = 443;
    private static UDPhelper udpHelper;
    public static boolean relaying = false;

    public static void relayInfo() throws Exception {

        if (Rendering.client.world == null) {
            return;
        }

        for (Entity entity : Rendering.client.world.getEntities()) {

            String username = entity.getName().getString();

            for (byte c : username.getBytes(StandardCharsets.UTF_8)) {

                if (c < 0x30 || c > 0x7E) {
                    username = "unknown";
                    break;
                }

            }

            if (username.isEmpty()) username = "unknown";

            if (entity.getUuid() == Rendering.client.player.getUuid()) {

                Player selfOut = new Player(entity.getUuid().toString(), username, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), entity.getEntity().getHealth());

                udpHelper.queueObject(selfOut);

            }

            else if (entity.isPlayer()) { if (Config.getWaypointAllowedEntity("players")) {

                if (!Waypoint.availableThreatLevels.containsKey(entity.getUuid().toString())) {

                    String headers = "{" +
                            "'Function' : 'get_player'," +
                            "'Database' : 'oppwatch'" +
                            "}";

                    String body = "{" +
                            "'username' : '" + entity.getStringifiedName() + "'" +
                            "}";

                    Request.get("http://" + Verification.caligulaEndpoint + "/functions", headers, body, (response) -> {
                        if (response.statusCode() == 200) {

                            JSONObject playerJson = new JSONObject(response.body());

                            JSONArray playerDataAg = playerJson.getJSONArray("data");

                            if (!playerDataAg.isEmpty()) {

                                JSONArray playerData = playerDataAg.getJSONArray(0);
                                int threat = playerData.getInt(1);

                                Waypoint.availableThreatLevels.put(entity.getUuid().toString(), threat);

                            }

                            else Waypoint.availableThreatLevels.put(entity.getUuid().toString(), 1);

                        }

                        else Waypoint.availableThreatLevels.put(entity.getUuid().toString(), 1);
                    });

                    Waypoint.availableThreatLevels.put(entity.getUuid().toString(), 1);
                }

                Waypoint.queueWaypointUpdate((float) entity.getX() - .5f, (float) entity.getY(), (float) entity.getZ() - .5f, Waypoint.Type.values()[Waypoint.availableThreatLevels.get(entity.getUuid().toString())], entity.getUuid().toString(), entity.getStringifiedName(), true);

            } }

            else if (entity.isLiving())            { if (Config.getWaypointAllowedEntity("mobs"))   Waypoint.queueWaypointUpdate((float) entity.getX() - .5f, (float) entity.getY(), (float) entity.getZ() - .5f, Waypoint.Type.NORMAL, entity.getUuid().toString(), entity.getStringifiedName(), true); }
            else if (entity instanceof ItemEntity) { if (Config.getWaypointAllowedEntity("items"))  Waypoint.queueWaypointUpdate((float) entity.getX() - .5f, (float) entity.getY(), (float) entity.getZ() - .5f, Waypoint.Type.NORMAL, entity.getUuid().toString(), entity.getStringifiedName(), true); }
            else                                   { if (Config.getWaypointAllowedEntity("blocks")) Waypoint.queueWaypointUpdate((float) entity.getX() - .5f, (float) entity.getY(), (float) entity.getZ() - .5f, Waypoint.Type.NORMAL, entity.getUuid().toString(), entity.getStringifiedName(), true); }
        }

        udpHelper.send();
        udpHelper.clearQueue();

    }

    public static void gatherTelemetry(UDPObject udpObject) throws Exception {

        if (Rendering.client.player == null) return;

        Iterator<Byte> buffer = udpObject.data.iterator();

        if (!buffer.hasNext()) return;
        String UUID = TypeGenerators.decodeUUID(buffer);

        if (UUID.equals(Rendering.client.player.getUuidAsString())) return;

        if (!buffer.hasNext()) return;

        String username = TypeGenerators.decodeString(buffer);

        float x = 0;
        float y = 0;
        float z = 0;

        if (udpObject.identifier == 1) {
            x = TypeGenerators.decodeFloat(buffer);
            y = TypeGenerators.decodeFloat(buffer);
            z = TypeGenerators.decodeFloat(buffer);
        }

        if (udpObject.identifier == 2) {
            x = TypeGenerators.decodeInt(buffer);
            y = TypeGenerators.decodeInt(buffer);
            z = TypeGenerators.decodeInt(buffer);
        }

        if (!buffer.hasNext()) return;

        int threat = buffer.next();

        if (Config.getWaypointAllowedEntity("players")) Waypoint.queueWaypointUpdate(x, y, z, Waypoint.Type.values()[threat], UUID, username, false);

        Clock clock = Clock.systemDefaultZone();
        Debug.incomingTimestamp = clock.instant().getNano();

    }

    public static void startStream() throws IOException, InterruptedException {

        //UDPClient.createConnection(host, port);
        //UDPClient.listen(Relaying::gatherTelemetry);

        if (udpHelper != null) return;

        HttpResponse<String> req = Request.get("http://170.187.207.133:9000/connect");

        udpHelper = new UDPhelper(host, Relay.UDPport);
        udpHelper.startRecieving(Relaying::gatherTelemetry);

        relaying = true;

    }

    private static void gatherTelemetry(NetworkObject networkObject) throws Exception {

        if (networkObject.identifier == 1)  {

            Player incomingPlayer = (Player) networkObject;

            if (Config.getWaypointAllowedEntity("players"))
                Waypoint.queueWaypointUpdate(
                        incomingPlayer.x - .5f,
                        incomingPlayer.y,
                        incomingPlayer.z - .5f,
                        Waypoint.Type.values()[incomingPlayer.getThreat()],
                        incomingPlayer.uuid,
                        incomingPlayer.username,
                        false
                );

        }

        Clock clock = Clock.systemDefaultZone();
        Debug.incomingTimestamp = clock.instant().getNano();

    }

    public static class TestEntity {
        public String UUID;

        public float x;
        public float y;
        public float z;

        public TestEntity(String UUID, float x, float y, float z) {
            this.UUID = UUID;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
        
    }
}
