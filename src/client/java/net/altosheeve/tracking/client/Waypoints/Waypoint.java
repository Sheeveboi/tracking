package net.altosheeve.tracking.client.Waypoints;

import net.altosheeve.tracking.client.Core.Rendering;
import net.altosheeve.tracking.client.Core.Values;
import net.altosheeve.tracking.client.Shapes.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.text.DecimalFormat;
import java.util.*;

public class Waypoint extends Shape {

    public enum Type {
        GOOD_GUY,
        NORMAL,
        SHITTER,
        HITLER,
        ALERT,
        PING,
        SNITCH,
        SNITCH_ALERT,
        PERMANENT
    }

    public static ArrayList<Waypoint> waypoints = new ArrayList<>();
    public static Layer fillLayer = new Layer("waypoint fill layer", Layer.Method.FILL_UNOCCLUDED);

    public Type type;
    public float importance;
    public float decayRate;
    public boolean overworld;

    public String username = "";

    public static void cleanWaypoints() {

        ArrayList<Waypoint> remove = new ArrayList<>();

        for (Waypoint waypoint : waypoints) {

            waypoint.importance -= waypoint.decayRate;

            if (waypoint.importance <= 0) {
                remove.add(waypoint);
                lineLayer.removeShape(waypoint);
                fillLayer.removeShape(waypoint);
            }
        }

        waypoints.removeAll(remove);

    }

    public static void updateWaypoint(float x, float y, float z, Type type, String UUID, String displayName) {

        for (Waypoint waypoint : waypoints) {
            if (Objects.equals(waypoint.UUID, UUID)) {

                if (waypoint.importance > Values.importanceRegistry(type)) return;
                if (waypoint.type.ordinal() < type.ordinal()) return;

                waypoint.username = displayName;
                waypoint.x = x;
                waypoint.y = y;
                waypoint.z = z;

                waypoint.type = type;

                waypoint.importance = Values.importanceRegistry(type);
                waypoint.decayRate = Values.decayRateRegistry(type);

                fillLayer.updateShape(waypoint.UUID, waypoint);

                return;
            }
        }

        Waypoint newWaypoint = new Waypoint(x, y, z, type, UUID, displayName);

        fillLayer.addShape(newWaypoint);
        waypoints.add(newWaypoint);

    }

    public Waypoint(float x, float y, float z, Type type, String UUID, String username) {

        super(x, y, z, UUID);

        this.type = type;
        this.importance = Values.importanceRegistry(type);
        this.decayRate = Values.decayRateRegistry(type);
        this.username = username;

        Circle testCircle = new Circle(0, 0, 0, this.UUID, .5F, 1);
        this.addShape(testCircle);

    }

    public static void drawText(VertexConsumerProvider.Immediate provider) {

        if (waypoints.isEmpty()) return;

        ArrayList<Waypoint> waypointsCopy = new ArrayList<>(waypoints);

        waypointsCopy.sort((a, b) -> Float.compare(Transforms.facingValue(b.x, b.y, b.z), Transforms.facingValue(a.x, a.y, a.z)));

        float scale = Values.textSizeRegistry(waypointsCopy.getFirst().type);
        Matrix4f spriteTransform = Transforms.getWorld3dSpriteTransform(waypointsCopy.getFirst().x, waypointsCopy.getFirst().y, waypointsCopy.getFirst().z, scale, -scale, scale);

        int y = 5;

        for (Waypoint waypoint : waypointsCopy) {

            if (Transforms.facingValue(waypoint.x, waypoint.y, waypoint.z) <= 1 - Values.focusThresholdRegistry(waypoint.type)) break;

            float dist = Transforms.distanceValue(waypoint.x, waypoint.y, waypoint.z);

            StringBuilder waypointInfo = new StringBuilder();
            DecimalFormat df = new DecimalFormat("#.##");
            waypointInfo.append(waypoint.username).append(" [").append(df.format(dist)).append("m]");

            float distanceStringWidth = -Rendering.client.textRenderer.getWidth(waypointInfo.toString()) / 2f;

            Rendering.client.textRenderer.draw(Text.literal(waypointInfo.toString()), distanceStringWidth, y, 0xffffffff, true, spriteTransform, provider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);

            y += 10;

        }
    }

    @Override
    public void fill(BufferBuilder buffer) {

        this.importance -= decayRate;

        this.activeTransform = Transforms.getWorld3dSpriteTransform(this.x, this.y, this.z, Values.scaleRegistry(this.type) * Values.waypointScale, Values.scaleRegistry(this.type) * Values.waypointScale, Values.scaleRegistry(this.type) * Values.waypointScale);

    }
}
