package net.altosheeve.tracking.client.Waypoints;

import net.altosheeve.tracking.client.Core.Rendering;
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

    public static Map<String, Float> availableYLevels = new HashMap<>();
    public static Map<String, Integer> availableThreatLevels = new HashMap<>();
    public static ArrayList<Waypoint> waypoints = new ArrayList<>();
    public static ArrayList<Waypoint> waypointQueue = new ArrayList<>();
    public static Layer fillLayer = new Layer("waypoint fill layer", Layer.Method.FILL_UNOCCLUDED);
    public static Layer lineLayer = new Layer("waypoint line layer", Layer.Method.LINE_OCCLUDED);

    public float importance     = .8f;
    public float decayRate      = .001f;
    public float scale          = .02f;
    public float focusThreshold = .01f;
    public float textSize       = .01f;
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

            while (true)
                try {
                    for (Waypoint queuedWaypoint : waypointQueue)
                        if (Objects.equals(waypoint.UUID, queuedWaypoint.UUID))
                            remove.add(waypoint);

                    break;
                } catch (Exception ignored) {}

        }

        removeWaypoints(remove);
        addWaypoints(waypointQueue);

        waypointQueue.clear();

    }

    public static void addWaypoint(Waypoint waypoint) {

        waypoints.add(waypoint);

        fillLayer.addShape(waypoint);
        lineLayer.addShape(waypoint);

    }

    public static void addWaypoints(ArrayList<Waypoint> waypointsB) {

        while (true)
            try {
                waypoints.addAll(waypointsB);

                for (Shape shape : waypointsB) fillLayer.addShape(shape);
                for (Shape shape : waypointsB) lineLayer.addShape(shape);

                break;

            } catch (Exception ignored) {}

    }

    public static void removeWaypoints(ArrayList<Waypoint> waypointsB) {

        while (true)
            try {
                waypoints.removeAll(waypointsB);

                for (Shape shape : waypointsB) fillLayer.removeShape(shape);
                for (Shape shape : waypointsB) lineLayer.removeShape(shape);

                break;

            } catch (Exception ignored) {}
    }

    public static void removeWaypoint(Waypoint waypoint) {

        waypoints.remove(waypoint);

        fillLayer.removeShape(waypoint);
        lineLayer.removeShape(waypoint);

    }

    public static Waypoint generateWaypoint(float x, float y, float z, Type type, String UUID, String displayName) {

        switch (type) {

            case GOOD_GUY -> { return new GoodGuy(x, y, z, UUID, displayName); }
            case NORMAL   -> { return new Normal(x, y, z, UUID, displayName);  }
            case SHITTER  -> { return new Shitter(x, y, z, UUID, displayName); }
            case HITLER   -> { return new Hitler(x, y, z, UUID, displayName);  }
            case SNITCH   -> { return new Snitch(x, y, z, UUID, displayName);  }

            case SNITCH_ALERT   -> { return new SnitchAlert(x, y, z, UUID, displayName); }

            default -> { return new Waypoint(x, y, z, UUID, displayName); }

        }

    }

    public static Waypoint generateWaypoint(Type type) {

        switch (type) {

            case GOOD_GUY -> { return new GoodGuy(0, 0, 0, java.util.UUID.randomUUID().toString(), "None"); }
            case NORMAL   -> { return new Normal(0, 0, 0, java.util.UUID.randomUUID().toString(), "None");  }
            case SHITTER  -> { return new Shitter(0, 0, 0, java.util.UUID.randomUUID().toString(), "None"); }
            case HITLER   -> { return new Hitler(0, 0, 0, java.util.UUID.randomUUID().toString(), "None");  }
            case SNITCH   -> { return new Snitch(0, 0, 0, java.util.UUID.randomUUID().toString(), "None");  }

            case SNITCH_ALERT   -> { return new SnitchAlert(0, 0, 0, java.util.UUID.randomUUID().toString(), "None"); }

            default -> { return new Waypoint(0, 0, 0, java.util.UUID.randomUUID().toString(), "None"); }

        }

    }

    public static void queueWaypointUpdate(float x, float y, float z, Type type, String UUID, String displayName, boolean ignoreY) {

        while (true) {

            try {

                float legalY = (float) Rendering.client.player.getY();
                if (!ignoreY) availableYLevels.put(UUID, y);
                if (availableYLevels.containsKey(UUID)) legalY = availableYLevels.get(UUID);
                //On civ, you are allowed to see the Y levels of clients that are willingly sending you that information

                for (Waypoint waypoint : waypoints)
                    if (waypoint != null && Objects.equals(waypoint.UUID, UUID)) {

                        if (waypoint.importance > generateWaypoint(type).importance) return;

                        break;

                    }

                for (Waypoint waypoint : waypointQueue) if (Objects.equals(waypoint.UUID, UUID)) return;

                Waypoint newWaypoint = generateWaypoint(x, legalY, z, type, UUID, displayName);
                waypointQueue.add(newWaypoint);

                break;

            }

            catch (Exception ignored) {}
        }

    }

    public Waypoint(float x, float y, float z, String UUID, String username) {
        super(x, y, z, UUID);
        this.username = username;
    }

    //TODO: make text layers in Shapes
    public static void drawText(VertexConsumerProvider.Immediate provider) {

        if (waypoints.isEmpty()) return;

        ArrayList<Waypoint> waypointsCopy = new ArrayList<>(waypoints);

        waypointsCopy.sort((a, b) -> Float.compare(Transforms.facingValue(b.x, b.y, b.z), Transforms.facingValue(a.x, a.y, a.z)));

        Waypoint focusedWaypoint = waypointsCopy.getFirst();

        Matrix4f spriteTransform = Transforms.getWorld3dSpriteTransform(focusedWaypoint.x, focusedWaypoint.y, focusedWaypoint.z, focusedWaypoint.textSize, -focusedWaypoint.textSize, focusedWaypoint.textSize);

        int y = 5;

        for (Waypoint waypoint : waypointsCopy) {

            if (Transforms.facingValue(waypoint.x, waypoint.y, waypoint.z) <= 1 - waypoint.focusThreshold) break;

            float dist = Transforms.distanceValue(waypoint.x, waypoint.y, waypoint.z);

            StringBuilder waypointInfo = new StringBuilder();
            DecimalFormat df = new DecimalFormat("#");
            waypointInfo.append(waypoint.username).append(" [").append(df.format(dist)).append("m]");

            float distanceStringWidth = -Rendering.client.textRenderer.getWidth(waypointInfo.toString()) / 2f;

            Rendering.client.textRenderer.draw(Text.literal(waypointInfo.toString()), distanceStringWidth, y, 0xffffffff, true, spriteTransform, provider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);

            y += 10;

        }
    }

    @Override
    public void fill(BufferBuilder buffer) {

        org.joml.Matrix4f spriteTransform = Transforms.getWorld3dSpriteTransform(this.x, this.y, this.z, this.scale * .02f, this.scale * .02f, this.scale * .02f);

        Circle circle = new Circle(0, 0, 0, this.UUID, .5F, 1);
        circle.parentLayer = this.parentLayer;
        circle.color(this.r, this.g, this.b, this.importance);
        circle.set(buffer, spriteTransform);

    }

    @Override
    public void line(BufferBuilder buffer) {

        float scale = Transforms.scalingFunction(100, this.x, this.y, this.z);

        buffer.vertex(this.activeTransform, this.finalX + .5f, this.finalY + scale - 1f, this.finalZ + .5f).color(this.r, this.g, this.b, this.importance);
        buffer.vertex(this.activeTransform, this.finalX + .5f, this.finalY + 10000, this.finalZ + .5f).color(this.r, this.g, this.b, this.importance);

        buffer.vertex(this.activeTransform, this.finalX + .5f, this.finalY - scale, this.finalZ + .5f).color(this.r, this.g, this.b, this.importance);
        buffer.vertex(this.activeTransform, this.finalX + .5f, this.finalY + -10000, this.finalZ + .5f).color(this.r, this.g, this.b, this.importance);

    }
}
