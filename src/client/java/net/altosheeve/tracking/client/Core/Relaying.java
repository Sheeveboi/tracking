package net.altosheeve.tracking.client.Core;

import net.altosheeve.tracking.client.Networking.TypeGenerators;
import net.altosheeve.tracking.client.Networking.UDPClient;
import net.altosheeve.tracking.client.Networking.UDPObject;
import net.altosheeve.tracking.client.Render.Rendering;
import net.altosheeve.tracking.client.Render.Waypoint;
import net.minecraft.entity.Entity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Relaying {

    public static String host = "170.187.207.133";
    public static int port = 443;

    public static void relayInfo() throws IOException {
        if (Rendering.client.world == null) {
            UDPClient.pushQueue();
            return;
        }

        for (int i = 0; i < Waypoint.waypoints.size(); i++) Waypoint.waypoints.get(i).importance -= Waypoint.waypoints.get(i).decayRate;

        for (Entity entity : Rendering.client.world.getEntities()) {
            if (entity.isPlayer()) {

                String username = entity.getName().getString();

                for (byte c : username.getBytes(StandardCharsets.UTF_8)) {

                    if (c < 0x30 || c > 0x7E) {
                        username = "unknown";
                        break;
                    }

                }

                if (username.isEmpty()) username = "unknown";

                UDPObject send = new UDPObject((byte) 0x1,
                        TypeGenerators.encodePlayer(
                                (float) entity.getX() - .5f,
                                (float) entity.getY() + 1.5f,
                                (float) entity.getZ() - .5f,
                                entity.getUuid(),
                                username));

                UDPClient.queueObject(send);

            }
        }

        UDPClient.pushQueue();
    }

    public static void gatherTelemetry(UDPObject udpObject) {

        if (Rendering.client.player == null) return;

        Iterator<Byte> buffer = udpObject.data.iterator();

        if (!buffer.hasNext()) return;
        String UUID = TypeGenerators.decodeUUID(buffer);

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

        Waypoint.updateWaypoint(x, (float) Rendering.client.player.getY() + 1.5f, z, Waypoint.Type.values()[threat], UUID, username);

    }

    public static void startStream() throws IOException {

        UDPClient.createConnection(host, port);
        UDPClient.listen(Relaying::gatherTelemetry);

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
    
    public static void main(String[] args) throws InterruptedException, IOException {

        startStream();

        UDPObject send1 = new UDPObject((byte) 0x1,
                TypeGenerators.encodePlayer(
                        (float) 1,
                        (float) 1,
                        (float) 1,
                        UUID.randomUUID(), "test"));

        while (true) {

            UDPClient.queueObject(send1);
            UDPClient.pushQueue();
            
            Thread.sleep(10);

        }
        
    }
}
