package net.altosheeve.tracking.client.Render;

import net.altosheeve.tracking.client.Core.Values;
import net.altosheeve.tracking.client.Shapes.Circle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.text.DecimalFormat;
import java.util.*;

public class Waypoint {

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

    public float x;
    public float y;
    public float z;

    public Type type;
    public float importance;
    public float decayRate;
    public boolean overworld;

    public String uuid = "";
    public String username = "";

    public static void updateWaypoint(float x, float y, float z, Type type, String UUID, String Username) {

        if (Rendering.client.player == null || UUID.toString().equals(Rendering.client.player.getUuidAsString())) return;

        for (Waypoint waypoint : waypoints) {
            if (UUID.equals(waypoint.uuid)) {

                if (waypoint.importance > Values.importanceRegistry(type)) return;
                if (waypoint.type.ordinal() < type.ordinal()) return;

                waypoint.username = Username;
                waypoint.x = x;
                waypoint.y = y;
                waypoint.z = z;

                waypoint.type = type;

                waypoint.importance = Values.importanceRegistry(type);
                waypoint.decayRate = Values.decayRateRegistry(type);

                return;

            }
        }

        waypoints.add(new Waypoint(x, y, z,type, UUID, Username));

    }

    public Waypoint(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.type = Type.GOOD_GUY;
        this.importance = 1;
        this.decayRate = 0;
    }

    public Waypoint(float x, float y, float z, Type type) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.type = type;
        this.importance = 1;
        this.decayRate = 0;
    }

    public Waypoint(float x, float y, float z, Type type, String uuid, String username) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.type = type;
        this.importance = Values.importanceRegistry(type);
        this.decayRate = Values.decayRateRegistry(type);;
        this.uuid = uuid;
        this.username = username;
    }

    public void drawGoodGuy(BufferBuilder buffer, Matrix4f spriteTransform) {
        Circle outerOutline = new Circle(0,0,0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance, .8f, 1);
        Circle innerCircle  = new Circle(0,0,0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance, 0, .6f);

        outerOutline.draw(buffer, spriteTransform);
        innerCircle.draw(buffer, spriteTransform);
    }

    public void drawNormal(BufferBuilder buffer, Matrix4f spriteTransform) {
        Circle outerOutline = new Circle(0,0,0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance, .9f, 1);
        Circle innerCircle  = new Circle(0,0,0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance, 0, .4f);

        outerOutline.draw(buffer, spriteTransform);
        innerCircle.draw(buffer, spriteTransform);
    }

    public void drawShitter(BufferBuilder buffer, Matrix4f spriteTransform) {
        Circle outerOutline = new Circle(0,0,0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance, .5f, 1);

        outerOutline.draw(buffer, spriteTransform);
    }

    public void drawHitler(BufferBuilder buffer, Matrix4f spriteTransform) {
        Circle outerOutline = new Circle(0,0,0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance, 0, 1);

        outerOutline.draw(buffer, spriteTransform);
    }

    public void drawSnitch(BufferBuilder buffer, Matrix4f spriteTransform) {
        buffer.vertex(spriteTransform, 0, .999f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, 0, .999f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, .999f, -.999f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, -.999f, -.999f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
    }

    public void drawSnitchAlert(BufferBuilder buffer, Matrix4f spriteTransform) {
        buffer.vertex(spriteTransform, 0, .999f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, 0, .999f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, .999f, -.999f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, -.999f, -.999f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
    }

    public void drawPing(BufferBuilder buffer, Matrix4f spriteTransform) {
        Circle firstRing  = new Circle(0, 0, 0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], 1, 1, .80f);
        Circle secondRing = new Circle(0, 0, 0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], 1, .60f, .40f);

        firstRing.draw(buffer, spriteTransform);
        secondRing.draw(buffer, spriteTransform);
    }

    public void drawAlert(BufferBuilder buffer, Matrix4f spriteTransform) {
        Circle firstRing  = new Circle(0, 0, 0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], 1, 1, .80f);
        Circle secondRing = new Circle(0, 0, 0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], 1, .60f, .40f);
        Circle thirdRing  = new Circle(0, 0, 0, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], 1, .20f, 0);

        firstRing.draw(buffer, spriteTransform);
        secondRing.draw(buffer, spriteTransform);
        thirdRing.draw(buffer, spriteTransform);
    }

    public void drawPermanent(BufferBuilder buffer, Matrix4f spriteTransform) {
        buffer.vertex(spriteTransform, 1, 1, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, .8f, .8f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, -.8f, .8f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, -1, 1, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);

        buffer.vertex(spriteTransform, 1, 1, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, .8f, .8f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, .8f, -.8f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, 1, -1, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);

        buffer.vertex(spriteTransform, -1, 1, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, -.8f, .8f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, -.8f, -.8f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, -1, -1, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);

        buffer.vertex(spriteTransform, 1, -1, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, .8f, -.8f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, -.8f, -.8f, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
        buffer.vertex(spriteTransform, -1, -1, 0).color(Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], importance);
    }

    public void drawShaft(BufferBuilder buffer, Matrix4f shaftTransform) {
        //Box shaft = new Box(0, -500, 0, 1, 9000, 1, Values.waypointRegistry(this.type)[0], Values.waypointRegistry(this.type)[1], Values.waypointRegistry(this.type)[2], this.importance / 2);
        //shaft.draw(buffer, shaftTransform);
    }

    public void drawPoint(BufferBuilder buffer) {

        this.importance -= decayRate;

        Matrix4f spriteTransform = Transforms.getWorld3dSpriteTransform(this.x, this.y, this.z, Values.waypointScale, Values.waypointScale, Values.waypointScale);
        Matrix4f shaftTransform = Transforms.getWorld3dTransform(this.x, this.y, this.z, Values.shaftScale, this.type);

        drawShaft(buffer, shaftTransform);

        switch (this.type) {
            case GOOD_GUY -> drawGoodGuy(buffer, spriteTransform);
            case NORMAL   -> drawNormal(buffer, spriteTransform);
            case SHITTER  -> drawShitter(buffer, spriteTransform);
            case HITLER   -> drawHitler(buffer, spriteTransform);

            case SNITCH         -> drawSnitch(buffer, spriteTransform);
            case SNITCH_ALERT   -> drawSnitchAlert(buffer, spriteTransform);

            case PING  -> drawPing(buffer, spriteTransform);
            case ALERT -> drawAlert(buffer, spriteTransform);

            case PERMANENT -> drawPermanent(buffer, spriteTransform);
        }

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

            Rendering.client.advanceValidatingTextRenderer.draw(Text.literal(waypointInfo.toString()), distanceStringWidth, y, 0xffffffff, true, spriteTransform, provider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);

            y += 10;

        }
    }
}
